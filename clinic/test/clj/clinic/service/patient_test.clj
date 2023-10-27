(ns clinic.service.patient-test
  (:require [clinic.factory :as factory]
            [clinic.fhir.client :as fc]
            [clinic.service.patient :as svc]
            [clinic.test-utils :as tu]
            [clojure.test :refer [deftest is testing]]))

(deftest create-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)]
    (with-redefs [fc/create! mocked-fn]
      (testing "with missing required param fields"
        (doseq [missing-field [:first-name :last-name :birth-date :gender :phone]]
          (is (= :invalid-params (->  (factory/create-params)
                                      (dissoc missing-field)
                                      ((partial svc/create! "test-server-url"))
                                      (tu/catch-thrown-data)
                                      (get :type))))))

      (testing "with invalid params"
        (doseq [[key & invalid-vals] [[:first-name " " ""]
                                      [:last-name " " ""]
                                      [:birth-date " " ""]
                                      [:gender "" " " "abc" "123"]
                                      [:phone "" " " "abc" "---"]]
                invalid-val invalid-vals]
          (is (= :invalid-params (->> (factory/create-params key invalid-val)
                                      (svc/create! "test-server-url")
                                      (tu/catch-thrown-data)
                                      (:type))))))

      (testing "with valid params"
        (reset! response-fn (fn [_ resource & _] {:status 201
                                                  :body (assoc resource :id "test-id")}))
        (doseq [missing-field [:marital-status :email nil]]
          (let [params (-> (factory/create-params)
                           (dissoc missing-field))
                patient (svc/create! "test-server-url" params)]
            (is (= "test-server-url" (@call-args 0)))
            (is (= "test-id" (patient :id)))
            (is (= (params :first-name) (patient :first-name)))
            (is (= (params :last-name) (patient :last-name)))
            (is (= (params :birth-date) (patient :birth-date)))
            (is (= (params :gender) (patient :gender)))
            (is (= (params :marital-status) (patient :marital-status)))
            (is (= (params :email) (patient :email)))
            (is (tu/digits-equal? (params :phone) (patient :phone))))))

      (testing "with upstream service non-20x response"
        (reset! response-fn (constantly {:status 400}))
        (is (= :upstream-error (->> (factory/create-params)
                                    (svc/create! "test-server-url")
                                    (tu/catch-thrown-data)
                                    (:type))))))))

(deftest get-all-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)]
    (with-redefs [fc/get-all mocked-fn]
      (testing "with invalid params"
        (doseq [[key & invalid-vals] [[:phone "" " "]
                                      [:offset "" " " "abc" "-"]
                                      [:count "" " " "abc" "-"]]
                invalid-val invalid-vals]
          (is (= :invalid-params (->> (factory/get-all-params key invalid-val)
                                      (svc/get-all "test-fhir-server")
                                      (tu/catch-thrown-data)
                                      (:type))))))

      (testing "with valid params"
        (reset! response-fn (fn [_ _ {count :_count}]
                              {:status 200
                               :body {:resourceType "Bundle"
                                      :entry (->> (repeatedly (parse-long count)
                                                              factory/fhir-patient)
                                                  (map #(do {:resource %})))}}))
        (doseq [missing-field [:phone :offset :count nil]]
          (let [params (-> (factory/get-all-params)
                           (dissoc missing-field))
                patients (svc/get-all "test-server-url" params)]
            (is (= "test-server-url" (@call-args 0)))
            (is (= (-> params
                       (get :count "10")
                       (parse-long))
                   (count patients))))))

      (testing "with upstream service error"
        (reset! response-fn (constantly {:status 400}))
        (is (= :upstream-error (->> (factory/get-all-params)
                                    (svc/get-all "test-server-url")
                                    (tu/catch-thrown-data)
                                    (:type))))))))

(deftest get-by-id-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)]
    (with-redefs [fc/get-by-id mocked-fn]
      (testing "with invalid params"
        (doseq [invalid-id ["" " " "abc"]]
          (is (= :invalid-params (->> (factory/get-all-params key invalid-id)
                                      (svc/get-by-id "test-fhir-server")
                                      (tu/catch-thrown-data)
                                      (:type))))))

      (testing "with valid params"
        (reset! response-fn (fn [_ _ id]
                              {:status 200
                               :body (factory/fhir-patient :id id)}))
        (let [patient (svc/get-by-id "test-server-url" "123")]
          (is (= "test-server-url" (@call-args 0)))
          (is (= "123" (@call-args 2)))
          (is (= "123" (patient :id)))))

      (testing "with Patient not found error"
        (reset! response-fn (constantly {:status 404}))
        (is (= :patient-not-found (->> (svc/get-by-id "test-server-url" "123")
                                       (tu/catch-thrown-data)
                                       (:type)))))

      (testing "with upstream service error"
        (reset! response-fn (constantly {:status 400}))
        (is (= :upstream-error (->> (svc/get-by-id "test-server-url" "123")
                                    (tu/catch-thrown-data)
                                    (:type))))))))
