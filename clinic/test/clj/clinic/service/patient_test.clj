(ns clinic.service.patient-test
  (:require [clinic.fhir.client :as fc]
            [clinic.service.patient :as svc]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test :refer [deftest is testing]]))

(defmacro catch-thrown-data [& body]
  `(try ~@body
        (catch clojure.lang.ExceptionInfo e# (ex-data e#))))

(defn- generate-create-params []
  (gen/generate (s/gen :clinic.service.patient/create-params)))

(deftest create-test
  (let [call-args (atom [])
        response-fn (atom (constantly nil))]
    (with-redefs [fc/create!  (fn [& args]
                                (reset! call-args (vec args))
                                (@response-fn (second args)))]
      (testing "with missing required param fields"
        (doseq [missing-field [:first-name :last-name :birth-date :gender]]
          (->  (generate-create-params)
               (dissoc missing-field)
               ((partial svc/create! "test-server-url"))
               (catch-thrown-data)
               (get :type)
               (= :invalid-params)
               (is))))

      (testing "with valid params"
        (reset! response-fn (fn [resource] {:status 201
                                            :body (assoc resource :id "test-id")}))
        (doseq [missing-field [:marital-status :email :phone nil]]
          (let [params (-> (generate-create-params)
                           (dissoc missing-field))
                patient (svc/create! "test-server-url" params)]
            (is (= "test-server-url" (@call-args 0)))
            (is (= "test-id" (patient :id)))
            (is (re-matches #"\d{3}-\d{3}-\d{3}" (patient :mrn)))
            (is (= (params :first-name) (patient :first-name)))
            (is (= (params :last-name) (patient :last-name)))
            (is (= (params :birth-date) (patient :birth-date)))
            (is (= (params :gender) (patient :gender)))
            (is (= (get params :marital-status "UNK") (patient :marital-status)))
            (is (= (params :email) (patient :email)))
            (is (= (params :phone) (patient :phone))))))

      (testing "with mrn conflict"
        (reset! response-fn (constantly {:status 200}))
        (-> (generate-create-params)
            ((partial svc/create! "test-server-url"))
            (catch-thrown-data)
            (get :type)
            (= :mrn-conflict)
            (is)))

      (testing "with upstream service non-20x response"
        (reset! response-fn (constantly {:status 400}))
        (-> (generate-create-params)
            ((partial svc/create! "test-server-url"))
            (catch-thrown-data)
            (get :type)
            (= :upstream-error)
            (is))))))
