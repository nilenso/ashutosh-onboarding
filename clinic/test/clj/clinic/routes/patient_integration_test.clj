(ns clinic.routes.patient-integration-test
  (:require [clinic.factory :as factory]
            [clinic.routes.core :as routes]
            [clinic.test-utils :as tu]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mr]
            [cheshire.core :as json]))

(defn- create-patient-request [body]
  (-> (mr/request :post "/api/v1/patients/")
      (mr/json-body body)))

(use-fixtures :once tu/load-config-fixture)
(use-fixtures :each tu/expunge-fhir-data-fixture)

(deftest create-patient-test
  (testing "with invalid request body"
    (doseq [missing-field [:first-name :last-name :birth-date :gender]]
      (is (= 400 (-> (factory/create-params)
                     (dissoc missing-field)
                     (create-patient-request)
                     (routes/handler)
                     (get :status))))))

  (testing "with valid request body"
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
      (is (= (params :phone) (body :phone)))
      (is (= (params :email) (body :email))))))
