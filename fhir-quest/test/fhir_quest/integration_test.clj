(ns fhir-quest.integration-test
  (:require [cheshire.core :as json]
            cli-matic.platform
            [clj-http.client :as client]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [fhir-quest.core :as fhir-quest]
            [fhir-quest.factory :as factory]
            [fhir-quest.fixture :as fixture]
            [java-time.api :as jt]))

(defn- get-records [db-file table]
  (jdbc/query {:connection-uri (str "jdbc:sqlite:" db-file)}
              (str "SELECT * FROM " table)))

(defn- get-aggregation-data [db-file agg-id]
  (some->> (get-records db-file "aggregation")
           (filter #(= agg-id (:id %)))
           (first)
           (:data_json)
           (#(json/parse-smile % true))))

(defn- set-aggregation-data! [db-file agg-id data]
  (jdbc/execute! {:connection-uri (str "jdbc:sqlite:" db-file)}
                 ["UPDATE aggregation SET data_json = ? WHERE id = ?"
                  (json/generate-smile data)
                  agg-id]))

(deftest ingest-test
  (testing "ingest command with empty directory"
    (with-redefs [cli-matic.platform/exit-script (constantly nil)]
      (fixture/with-tmp-dir
        (fn [tmp-dir]
          (let [db-file (str (.getCanonicalPath tmp-dir) "/ingest-test.db")]
            (fhir-quest/-main "-d" db-file
                              "ingest"
                              "-i" (.getCanonicalPath tmp-dir))
            (is (empty? (get-aggregation-data db-file "patient-age-group")))
            (is (empty? (get-aggregation-data db-file "patient-language-group")))
            (is (empty? (get-aggregation-data db-file "patient-marital-status-group"))))))))

  (testing "ingest command with Patient resources"
    (with-redefs [cli-matic.platform/exit-script (constantly nil)]
      (jt/with-clock (jt/mock-clock (jt/instant "2000-01-01T00:00:00+05:30"))
        (fixture/with-tmp-dir
          (fn [tmp-dir]
            (spit (io/file tmp-dir "1.json")
                  (json/generate-string (factory/fhir-patient-bundle 10
                                                                     "1989-12-01"
                                                                     "S"
                                                                     "en-IN")))
            (spit (io/file tmp-dir "2.json")
                  (json/generate-string (factory/fhir-patient-bundle 10
                                                                     "1980-05-01"
                                                                     "M"
                                                                     "en-IN")))
            (spit (io/file tmp-dir "3.json")
                  (json/generate-string (factory/fhir-patient-bundle 10
                                                                     "1970-01-01"
                                                                     "S"
                                                                     "en-GB")))

            (let [db-file (str (.getCanonicalPath tmp-dir) "/ingest-test.db")]
              (fhir-quest/-main "-d" db-file
                                "ingest"
                                "-i" (.getCanonicalPath tmp-dir))
              (is (= [{:label "Children", :value 10} {:label "Adults", :value 20}]
                     (get-aggregation-data db-file "patient-age-group")))
              (is (= [{:label "en-IN", :value 20} {:label "en-GB", :value 10}]
                     (get-aggregation-data db-file "patient-language")))
              (is (= [{:label "Single", :value 20} {:label "Married", :value 10}]
                     (get-aggregation-data db-file "patient-marital-status")))))))))

  (testing "ingest command with Encounter resources"
    (with-redefs [cli-matic.platform/exit-script (constantly nil)]
      (fixture/with-tmp-dir
        (fn [tmp-dir]
          (spit (io/file tmp-dir "1.json")
                (json/generate-string (factory/fhir-encounter-bundle 10
                                                                     (random-uuid)
                                                                     "2000-01-01T00:00:00+05:30"
                                                                     "2000-01-01T00:10:00+05:30")))
          (spit (io/file tmp-dir "2.json")
                (json/generate-string (factory/fhir-encounter-bundle 10
                                                                     (random-uuid)
                                                                     "2000-01-02T12:00:00+05:30"
                                                                     "2000-01-02T14:30:00+05:30")))
          (spit (io/file tmp-dir "3.json")
                (json/generate-string (factory/fhir-encounter-bundle 10
                                                                     (random-uuid)
                                                                     "2000-01-03T16:00:00+05:30"
                                                                     "2000-01-03T20:00:00+05:30")))
          (let [db-file (str (.getCanonicalPath tmp-dir) "/ingest-test.db")]
            (fhir-quest/-main "-d" db-file
                              "ingest"
                              "-i" (.getCanonicalPath tmp-dir))

            (is (= [{:label "Average (minutes)", :value 133}]
                   (get-aggregation-data db-file "encounter-duration-avg")))
            (is (= [{:label "< 15 mins", :value 1} {:label "2-4 hours", :value 2}]
                   (get-aggregation-data db-file "patient-encounter-duration-groups")))))))))

(defn wait-for-server [probe-url]
  (try (-> probe-url
           (client/get {:throw-exceptions false})
           (get :status)
           (not= 200))
       (catch java.net.ConnectException _ (do
                                            (Thread/sleep 1000)
                                            (wait-for-server probe-url)))))

(defn- with-serve [db-file f]
  (with-redefs [cli-matic.platform/exit-script (constantly nil)]
    (let [t  (Thread. #(fhir-quest/-main "-d" db-file
                                         "serve"
                                         "-p" "3000"))]
      (.start t)
      (try (wait-for-server "http://localhost:3000")
           (f)
           (finally (.interrupt t))))))

(deftest serve-test
  (testing "serve command without chart data"
    (fixture/with-tmp-dir
      (fn [tmp-dir]
        (let [db-file (str (.getCanonicalPath tmp-dir) "/serve-test.db")]
          (with-serve db-file
            (fn []
              (let [aggs (-> "http://localhost:3000/api/v1/aggregation"
                             (client/get {:as :json})
                             (get :body))]
                (is (seq aggs))
                (doseq [{agg-id :id} aggs]
                  (-> (str "http://localhost:3000/api/v1/aggregation/" agg-id "/chart")
                      (client/get {:throw-exceptions false})
                      (get :status)
                      (= 404)
                      (is))))))))))

  (testing "serve command with chart data"
    (fixture/with-tmp-dir
      (fn [tmp-dir]
        (let [db-file (str (.getCanonicalPath tmp-dir) "/serve-test.db")]
          (with-serve db-file
            (fn []
              (let [aggs (-> "http://localhost:3000/api/v1/aggregation"
                             (client/get {:as :json})
                             (get :body))]
                (is (seq aggs))
                (doseq [{agg-id :id} aggs]
                  ;; insert chart data
                  (set-aggregation-data! db-file agg-id [{:label "test" :value 10}])

                  (-> (str "http://localhost:3000/api/v1/aggregation/" agg-id "/chart")
                      (client/get {:as :json})
                      (get-in [:body :data])
                      (= [{:label "test" :value 10}])
                      (is)))))))))))
