(ns clinic.routes.patient-test
  (:require [clinic.routes.core :as routes]
            [clinic.service.patient :as svc]
            [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mr]))

(defn- create-patient-request []
  (-> (mr/request :post "/api/v1/patients/")
      (mr/json-body {:key "request-val"})
      (assoc :config {:fhir-server-base-url "test-fhir-server-url"})))

(deftest create-patient-test
  (let [call-args (atom [])
        response-fn (atom (constantly nil))]
    (with-redefs [svc/create! (fn [& args]
                                (reset! call-args (vec args))
                                (@response-fn))]
      (testing "with no service errors"
        (reset! response-fn (fn [] {:key "response-val"}))
        (let [response (routes/handler (create-patient-request))]
          (is (= "test-fhir-server-url" (@call-args 0)))
          (is (= {:key "request-val"} (@call-args 1)))
          (is (= 201 (response :status)))))

      (testing "with invalid params service error"
        (reset! response-fn #(throw (ex-info "test-error"
                                             {:type :invalid-params})))
        (is (= 400 (:status (routes/handler (create-patient-request))))))

      (testing "with mrn conflict service error"
        (reset! response-fn #(throw (ex-info "test-error"
                                             {:type :mrn-conflict})))
        (is (= 503 (:status (routes/handler (create-patient-request))))))

      (testing "with unknown service error"
        (reset! response-fn #(throw (RuntimeException. "test-error")))
        (is (= 500 (:status (routes/handler (create-patient-request)))))))))
