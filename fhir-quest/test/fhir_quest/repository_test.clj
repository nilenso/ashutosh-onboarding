(ns fhir-quest.repository-test
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [fhir-quest.factory :as factory]
            [fhir-quest.fixture :as fixture]
            [fhir-quest.repository :as repo]))

(defn- assert-aggregation-dbo-equals-domain-obj [dbo domain]
  (is (= (get dbo :id) (get domain :id)))
  (is (= (get dbo :description) (get domain :description)))
  (is (= (get dbo :chart_type) (get domain :chart_type)))
  (is (= (json/parse-smile (get dbo :data_json) true) (get domain :data))))

(defn- get-record-by-id [db-conn table id]
  (->> id
       (vector (str "SELECT * FROM " table " WHERE id = ?"))
       (jdbc/query db-conn)
       (first)))

(deftest list-aggregations-test
  (testing "with no records in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (is (empty? (repo/list-aggregations db-conn 0 -1))))))

  (testing "with one record"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [want (factory/aggregation-dbo)]
          (jdbc/insert! db-conn :aggregation want)
          (let [records (repo/list-aggregations db-conn 0 -1)]
            (is (= 1 (count records)))
            (assert-aggregation-dbo-equals-domain-obj want (first records)))))))

  (testing "with some records, offsets and limits"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [aggregations (repeatedly 5 factory/aggregation-dbo)]
          (jdbc/insert-multi! db-conn :aggregation aggregations)
          (doseq [[dbo domain] (zipmap aggregations
                                       (repo/list-aggregations db-conn 0 -1))]
            (assert-aggregation-dbo-equals-domain-obj dbo domain))

          (is (= 5 (count (repo/list-aggregations db-conn 0 -1))))
          (is (= 3 (count (repo/list-aggregations db-conn 2 -1))))
          (is (= 1 (count (repo/list-aggregations db-conn 0 1))))
          (is (= 2 (count (repo/list-aggregations db-conn 2 2)))))))))

(deftest get-aggregation-test
  (testing "with missing record in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (jdbc/insert! db-conn :aggregation (factory/aggregation-dbo))
        (is (nil? (repo/get-aggregation db-conn "non-existing-id"))))))

  (testing "with corresponding record in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [want (factory/aggregation-dbo)]
          (jdbc/insert! db-conn :aggregation want)
          (->> (get want :id)
               (repo/get-aggregation db-conn)
               (assert-aggregation-dbo-equals-domain-obj want)))))))

(defn- assert-aggregation-dbos-are-equal [want got]
  (is (= (get want :id) (get got :id)))
  (is (= (get want :description) (get got :description)))
  (is (= (get want :chart_type) (get got :chart_type)))
  (is (= (seq (get want :data_json)) (seq (get got :data_json)))))

(deftest update-aggregation-data-test
  (testing "with existing records and non-existing id"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [aggregations (repeatedly 5 factory/aggregation-dbo)]
          (jdbc/insert-multi! db-conn :aggregation aggregations)
          (repo/update-aggregation-data! db-conn "non-existing-id" [])
          (doseq [want aggregations]
            (->> (get want :id)
                 (get-record-by-id db-conn "aggregation")
                 (assert-aggregation-dbos-are-equal want)))))))

  (testing "with existing records and existing id"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [immutating-aggs (repeatedly 5 factory/aggregation-dbo)
              mutating-agg (factory/aggregation-dbo)
              all-aggs (cons mutating-agg immutating-aggs)
              mutating-id (get mutating-agg :id)
              updated-data [{:label "abc" :value 10}]
              mutated-agg (assoc mutating-agg
                                 :data_json
                                 (json/generate-smile updated-data))]
          (jdbc/insert-multi! db-conn :aggregation all-aggs)
          (repo/update-aggregation-data! db-conn mutating-id updated-data)
          (doseq [want immutating-aggs]
            (->> (get want :id)
                 (get-record-by-id db-conn "aggregation")
                 (assert-aggregation-dbos-are-equal want)))
          (->> (get mutating-agg :id)
               (get-record-by-id db-conn "aggregation")
               (assert-aggregation-dbos-are-equal mutated-agg)))))))

(deftest list-encounters-test
  (testing "with no records in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (is (empty? (repo/list-encounters db-conn))))))

  (testing "with some records in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [want (repeatedly 5 factory/encounter-dbo)]
          (jdbc/insert-multi! db-conn :encounter want)
          (is (= want (repo/list-encounters db-conn))))))))

(deftest list-patients-test
  (testing "with no records in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (is (empty? (repo/list-patients db-conn))))))

  (testing "with some records in the database"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [want (repeatedly 5 factory/patient-dbo)]
          (jdbc/insert-multi! db-conn :patient want)
          (is (= want (repo/list-patients db-conn))))))))

(deftest save-encounter-test
  (testing "without overwriting"
    (fixture/with-tmp-db
      (fn [db-conn]
        (repo/save-encounter! db-conn "test-id" "test-subject-id" 1000)
        (is (= {:id "test-id"
                :subject_id "test-subject-id"
                :duration_ms 1000}
               (get-record-by-id db-conn "encounter" "test-id"))))))

  (testing "with overwriting"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [immutating-encounters (repeatedly 5 factory/encounter-dbo)
              mutating-encounter (factory/encounter-dbo)
              all-encounters (cons mutating-encounter immutating-encounters)
              mutating-id (get mutating-encounter :id)]
          (jdbc/insert-multi! db-conn :encounter all-encounters)
          (repo/save-encounter! db-conn mutating-id "new-subject-id" 9999)
          (doseq [{id :id :as want} immutating-encounters]
            (is (= want (get-record-by-id db-conn "encounter" id))))
          (let [got (get-record-by-id db-conn "encounter" mutating-id)]
            (is (= "new-subject-id" (get got :subject_id)))
            (is (= 9999 (get got :duration_ms)))))))))

(deftest save-patient-test
  (testing "without overwriting"
    (fixture/with-tmp-db
      (fn [db-conn]
        (repo/save-patient! db-conn "test-id" "2000-01-01" "en-IN" "S")
        (is (= {:id "test-id"
                :birth_date "2000-01-01"
                :language "en-IN"
                :marital_status "S"}
               (get-record-by-id db-conn "patient" "test-id"))))))

  (testing "with overwriting"
    (fixture/with-tmp-db
      (fn [db-conn]
        (let [immutating-patients (repeatedly 5 factory/patient-dbo)
              mutating-patient (factory/patient-dbo)
              all-patients (cons mutating-patient immutating-patients)
              mutating-id (get mutating-patient :id)]
          (jdbc/insert-multi! db-conn :patient all-patients)
          (repo/save-patient! db-conn mutating-id "2001-02-03" "en-GB" "W")
          (doseq [{id :id :as want} immutating-patients]
            (is (= want (get-record-by-id db-conn "patient" id))))
          (let [got (get-record-by-id db-conn "patient" mutating-id)]
            (is (= "2001-02-03" (get got :birth_date)))
            (is (= "en-GB" (get got :language)))
            (is (= "W" (get got :marital_status)))))))))
