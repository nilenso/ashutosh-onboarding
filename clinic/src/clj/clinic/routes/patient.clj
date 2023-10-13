(ns clinic.routes.patient
  (:require [clinic.service.patient :as svc]
            [compojure.core :refer [defroutes POST]]
            [ring.util.response :as r]))

(defn- create-patient! [{{fhir-server-url :fhir-server-base-url} :config
                         content-type :content-type
                         :as request}]
  (let [params (case content-type
                 "application/x-www-form-urlencoded" (request :params)
                 "application/json" (request :body)
                 nil)]
    (try
      (-> (svc/create! fhir-server-url params)
          (r/response)
          (r/status 201))
      (catch Exception e
        (case (:type (ex-data e))
          :invalid-params (r/status 400)
          :mrn-conflict (r/status 503)
          (throw e))))))

(defroutes handler
  (POST "/" _ create-patient!))
