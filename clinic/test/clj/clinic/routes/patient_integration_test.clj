(ns clinic.routes.patient-integration-test
  (:require [cheshire.core :as json]
            [clinic.factory :as factory]
            [clinic.routes.core :as routes]
            [clinic.test-utils :as tu]
            [clinic.utils :as u]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mr]))

(use-fixtures :once tu/load-config-fixture)

(defn- create-patient-request [body]
  (-> (mr/request :post "/api/v1/patients/")
      (mr/json-body body)))

(deftest create-patient-test
  (testing "with missing params in request body"
    (tu/expunge-fhir-data!)
    (doseq [missing-field [:first-name :last-name :birth-date :gender]]
      (is (= 400 (-> (factory/create-params)
                     (dissoc missing-field)
                     (create-patient-request)
                     (routes/handler)
                     (:status))))))

  (testing "with invalid params in request body"
    (tu/expunge-fhir-data!)
    (doseq [[key & invalid-vals] [[:first-name " " ""]
                                  [:last-name " " ""]
                                  [:birth-date " " ""]
                                  [:gender "" " " "abc" "123"]
                                  [:phone "" " " "abc" "---"]]
            invalid-val invalid-vals]
      (is (= 400 (-> (factory/create-params key invalid-val)
                     (create-patient-request)
                     (routes/handler)
                     (:status))))))

  (testing "with valid request body"
    (tu/expunge-fhir-data!)
    (let [params (factory/create-params)
          {status :status
           body :body} (-> params
                           (create-patient-request)
                           (routes/handler)
                           (update :body json/parse-string true))]
      (is (= 201 status))
      (is (contains? body :id))
      (is (= (params :first-name) (body :first-name)))
      (is (= (params :last-name) (body :last-name)))
      (is (= (params :birth-date) (body :birth-date)))
      (is (= (params :gender) (body :gender)))
      (is (= (params :marital-status) (body :marital-status)))
      (is (tu/digits-equal? (params :phone) (body :phone)))
      (is (= (params :email) (body :email))))))

(defn- list-patients-request [params]
  (-> (mr/request :get "/api/v1/patients" params)))

(deftest list-patients-test
  (testing "with invalid query params"
    (tu/expunge-fhir-data!)
    (doseq [[key & invalid-vals] [[:phone "" " "]
                                  [:offset "" " " "abc" "-"]
                                  [:count "" " " "abc" "-"]]
            invalid-val invalid-vals]
      (is (= 400 (-> (factory/get-all-params key invalid-val)
                     (list-patients-request)
                     (routes/handler)
                     (:status))))))

  (testing "with valid phone filter in query params"
    (tu/expunge-fhir-data!)
    (let [phones (repeatedly 5 factory/rand-phone)]
      (doseq [phone phones]
        (tu/create-fhir-patient! (factory/fhir-patient :phone
                                                       (u/extract-digits phone))))
      (doseq [phone phones]
        (let [{:keys [status body]} (-> {:phone phone}
                                        (list-patients-request)
                                        (routes/handler)
                                        (update :body json/parse-string true))]
          (is (= 200 status))
          (doseq [patient body]
            (is (= (u/extract-digits phone) (patient :phone))))))))

  (testing "with valid offset and count in query params"
    (tu/expunge-fhir-data!)
    (doseq [patient (repeatedly 5 factory/fhir-patient)]
      (tu/create-fhir-patient! patient))

    (doseq [[params expected-result-count] [[{:offset 0 :count 1} 1]
                                            [{:offset 10 :count 10} 0]
                                            [{:offset 4 :count 10} 1]
                                            [{:offset 3 :count 10} 2]]]
      (let [{:keys [status body]} (-> params
                                      (list-patients-request)
                                      (routes/handler)
                                      (update :body json/parse-string true))]
        (is (= 200 status))
        (is (= expected-result-count (count body)))))))

(defn- get-patient-request [id]
  (->> (str "/api/v1/patients/" id)
       (mr/request :get)))

(deftest get-patient-test
  (testing "with invalid resource id in path param"
    (tu/expunge-fhir-data!)
    (is (= 400 (-> "%20%20"
                   (get-patient-request)
                   (routes/handler)
                   (:status)))))

  (testing "with non-existing resource id in path param"
    (tu/expunge-fhir-data!)
    (is (= 404 (-> (get-patient-request "1")
                   (routes/handler)
                   (:status)))))

  (testing "with valid resource id in path param"
    (tu/expunge-fhir-data!)
    (let [patient-id (-> (factory/fhir-patient)
                         (tu/create-fhir-patient!)
                         (get-in [:body :id]))
          {:keys [status body]} (-> patient-id
                                    (get-patient-request)
                                    (routes/handler)
                                    (update :body json/parse-string true))]
      (is (= 200 status))
      (is (= patient-id (body :id))))))
