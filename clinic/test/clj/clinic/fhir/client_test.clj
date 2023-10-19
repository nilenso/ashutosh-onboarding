(ns clinic.fhir.client-test
  (:require [clinic.fhir.client :as client]
            [clj-http.client :as c]
            [clojure.test :refer [deftest is testing]]
            [clinic.test-utils :as tu]
            [cheshire.core :as json]))

(deftest create-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)]
    (reset! response-fn (fn [_ params] {:status 201
                                        :headers (params :headers)
                                        :body (params :body)}))
    (with-redefs [c/post mocked-fn]
      (testing "with Test resource"
        (let [resp (client/create! "http://test.base.url/fhir"
                                   {:resourceType "Test"
                                    :key "resource-val"}
                                   {:header "header-val"})]
          (is (= "http://test.base.url/fhir/Test" (@call-args 0)))
          (is (= "resource-val" (get-in resp [:body :key])))
          (is (= "header-val" (get-in @call-args [1 :headers :header])))))

      (testing "with Bundle resource"
        (let [resp (client/create! "http://test.base.url/fhir"
                                   {:resourceType "Bundle"
                                    :key "resource-val"}
                                   {:header "header-val"})]
          (is (= "http://test.base.url/fhir" (@call-args 0)))
          (is (= "resource-val" (get-in resp [:body :key])))
          (is (= "header-val" (get-in @call-args [1 :headers :header]))))))))

(deftest get-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)]
    (reset! response-fn (fn [_ params] {:status 200
                                        :body (-> (params :query-params)
                                                  (json/generate-string))}))
    (with-redefs [c/get mocked-fn]
      (testing "with Test response"
        (let [resp (client/get-all "http://test.base.url/fhir"
                                   "Test"
                                   {:key "query-val"})]
          (is (= "http://test.base.url/fhir/Test" (@call-args 0)))
          (is (= "query-val" (get-in resp [:body :key]))))))))
