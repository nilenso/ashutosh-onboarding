(ns fhir-quest.http-test
  (:require [clojure.test :refer [deftest is testing]]
            [fhir-quest.service :as svc]
            [ring.mock.request :as mr]
            [fhir-quest.http :as http]
            [cheshire.core :as json]
            [fhir-quest.factory :as factory]))

(deftest list-aggregations-test
  (testing "with no aggregations"
    (with-redefs [svc/list-aggregations (constantly nil)]
      (let [resp (-> (mr/request :get "/api/v1/aggregation")
                     (http/routes))]
        (is (= 200 (:status resp)))
        (is (= "application/json" (get-in resp [:headers "Content-Type"])))
        (is (empty? (json/parse-string (:body resp)))))))

  (testing "with some aggregations"
    (let [want (->> factory/aggregation
                    (repeatedly 5)
                    (map #(select-keys % [:id :description])))]
      (with-redefs [svc/list-aggregations (constantly want)]
        (let [resp (-> (mr/request :get "/api/v1/aggregation")
                       (http/routes))]
          (is (= 200 (:status resp)))
          (is (= "application/json" (get-in resp [:headers "Content-Type"])))
          (is (= want (json/parse-string (:body resp) true))))))))

(deftest get-aggregation-chart-test
  (testing "with non-existing aggregation id"
    (with-redefs [svc/get-aggregation-chart (constantly nil)]
      (is (= 404 (-> (mr/request :get "/api/v1/aggregation/test-agg-id/chart")
                     (http/routes)
                     (get :status))))))

  (testing "with existing aggregation"
    (let [want {:type "test-chart"
                :data [{:label "test-label" :value 99}]}]
      (with-redefs [svc/get-aggregation-chart (constantly want)]
        (let [resp (-> (mr/request :get "/api/v1/aggregation/test-agg-id/chart")
                       (http/routes))]
          (is (= 200 (:status resp)))
          (is (= "application/json" (get-in resp [:headers "Content-Type"])))
          (is (= want (json/parse-string (:body resp) true))))))))

(deftest wrap-db-spec-test
  (testing "with a db-spec"
    (-> "test-db-spec"
        (http/wrap-db-spec (fn [request]
                             (is (= "test-db-spec"
                                    (:db-spec request)))))
        (#(% (mr/request :get "/"))))))
