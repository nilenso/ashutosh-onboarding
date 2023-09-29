(ns fhir-quest.service-test
  (:require [clojure.test :refer [deftest is testing]]
            [fhir-quest.repository :as repo]
            [fhir-quest.service :as svc]
            [fhir-quest.factory :as factory]))

(deftest list-aggregations-test
  (testing "with no data in the repository"
    (with-redefs [repo/list-aggregations (constantly [])]
      (is (empty? (svc/list-aggregations nil)))))

  (testing "with some data in the repository"
    (let [want (repeatedly 5 #(factory/aggregation))]
      (with-redefs [repo/list-aggregations (constantly want)]
        (doseq [[want got] (zipmap want
                                   (svc/list-aggregations nil))]
          (is (= (get want :id) (get got :id)))
          (is (= (get want :description) (get got :description))))))))

(deftest get-aggregation-chart-test
  (testing "with no data in the repository"
    (with-redefs [repo/get-aggregation (constantly nil)]
      (is (nil? (svc/get-aggregation-chart nil "test-agg-id")))))

  (testing "with some data in the repository"
    (let [want (factory/aggregation)]
      (with-redefs [repo/get-aggregation (constantly want)]
        (let [got (svc/get-aggregation-chart nil "test-agg-id")]
          (is (= (get want :chart_type) (get got :type)))
          (is (= (get want :data) (get got :data))))))))
