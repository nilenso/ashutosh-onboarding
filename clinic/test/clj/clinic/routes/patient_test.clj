(ns clinic.routes.patient-test
  (:require [cheshire.core :as json]
            [clinic.routes.core :as routes]
            [clinic.service.patient :as svc]
            [clinic.test-utils :as tu]
            [clojure.test :refer [deftest is testing]]
            [clojure.tools.logging :as log]
            [clojure.tools.logging.impl :as log-impl]
            [ring.mock.request :as mr]))

(deftest create-patient-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)
        create-patient-req (-> (mr/request :post "/api/v1/patients/")
                               (mr/json-body {:key "request-val"})
                               (assoc :config {:fhir-server-base-url "test-fhir-server-url"}))]
    (with-redefs [svc/create! mocked-fn]
      (testing "with no service errors"
        (reset! response-fn (constantly {:key "response-val"}))
        (let [response (routes/handler create-patient-req)]
          (is (= "test-fhir-server-url" (@call-args 0)))
          (is (= {:key "request-val"} (@call-args 1)))
          (is (= 201 (response :status)))
          (is (= {:key "response-val"} (-> (response :body)
                                           (json/parse-string true))))))

      (testing "with invalid params service error"
        (reset! response-fn (fn [& _] (throw (ex-info "test-error"
                                                      {:type :invalid-params}))))
        (is (= 400 (:status (routes/handler create-patient-req)))))

      (testing "with unknown service error"
        (reset! response-fn (fn [& _] (throw (RuntimeException. "test-error"))))
        (is (= 500 (binding [log/*logger-factory* log-impl/disabled-logger-factory]
                     (:status (routes/handler create-patient-req)))))))))

(deftest list-patients-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)
        query-params {:phone "0" :offset "1" :count "2"}
        list-patients-req (-> (mr/request :get "/api/v1/patients/" query-params)
                              (assoc :config {:fhir-server-base-url "test-fhir-server-url"}))]
    (with-redefs [svc/get-all mocked-fn]
      (testing "with no service errors"
        (reset! response-fn (constantly {:key "response-val"}))
        (let [response (routes/handler list-patients-req)]
          (is (= "test-fhir-server-url" (@call-args 0)))
          (is (= query-params (@call-args 1)))
          (is (= 200 (response :status)))
          (is (= {:key "response-val"} (-> (response :body)
                                           (json/parse-string true))))))

      (testing "with invalid params service error"
        (reset! response-fn (fn [& _] (throw (ex-info "test-error"
                                                      {:type :invalid-params}))))
        (is (= 400 (:status (routes/handler list-patients-req)))))

      (testing "with unknown service error"
        (reset! response-fn (fn [& _] (throw (RuntimeException. "test-error"))))
        (is (= 500 (binding [log/*logger-factory* log-impl/disabled-logger-factory]
                     (:status (routes/handler list-patients-req)))))))))

(deftest get-patient-test
  (let [[call-args response-fn mocked-fn] (tu/mock-fn)
        get-patient-req (-> (mr/request :get "/api/v1/patients/123")
                            (assoc :config {:fhir-server-base-url "test-fhir-server-url"}))]
    (with-redefs [svc/get-by-id mocked-fn]
      (testing "with no service errors"
        (reset! response-fn (constantly {:key "response-val"}))
        (let [response (routes/handler get-patient-req)]
          (is (= "test-fhir-server-url" (@call-args 0)))
          (is (= "123" (@call-args 1)))
          (is (= 200 (response :status)))
          (is (= {:key "response-val"} (-> (response :body)
                                           (json/parse-string true))))))

      (testing "with invalid params service error"
        (reset! response-fn (fn [& _] (throw (ex-info "test-error"
                                                      {:type :invalid-params}))))
        (is (= 400 (:status (routes/handler get-patient-req)))))

      (testing "with unknown service error"
        (reset! response-fn (fn [& _] (throw (RuntimeException. "test-error"))))
        (is (= 500 (binding [log/*logger-factory* log-impl/disabled-logger-factory]
                     (:status (routes/handler get-patient-req)))))))))
