(ns clinic.fhir.client-test
  (:require [clinic.fhir.client :as client]
            [clj-http.client :as c]
            [clojure.test :refer [deftest is testing]]))

(deftest create-test
  (let [post-params (atom [])]
    (with-redefs [c/post (fn [& args]
                           (reset! post-params (vec args))
                           {:status 201
                            :headers (get-in @post-params [1 :headers])
                            :body (get-in @post-params [1 :body])})]
      (testing "with Test resource"
        (let [resp (client/create! "http://test.base.url/fhir"
                                   {:resourceType "Test"
                                    :key "resource-val"}
                                   {:header "header-val"})]
          (is (= "http://test.base.url/fhir/Test" (@post-params 0)))
          (is (= "resource-val" (get-in resp [:body :key])))
          (is (= "header-val" (get-in @post-params [1 :headers :header])))))

      (testing "with Bundle resource"
        (let [resp (client/create! "http://test.base.url/fhir"
                                   {:resourceType "Bundle"
                                    :key "resource-val"}
                                   {:header "header-val"})]
          (is (= "http://test.base.url/fhir" (@post-params 0)))
          (is (= "resource-val" (get-in resp [:body :key])))
          (is (= "header-val" (get-in @post-params [1 :headers :header]))))))))
