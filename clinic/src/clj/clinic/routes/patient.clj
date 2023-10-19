(ns clinic.routes.patient
  (:require [clinic.service.patient :as svc]
            [compojure.core :refer [defroutes GET POST]]
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
          (throw e))))))

(defn- list-patients [{{fhir-server-url :fhir-server-base-url} :config
                       {:keys [phone offset count]} :params}]
  (try
    ;; `params` in request contains form + query params. Therefore, destructure
    ;; only what is needed.
    (-> (svc/get-all fhir-server-url {:phone phone
                                      :offset offset
                                      :count count})
        (r/response)
        (r/status 200))
    (catch Exception e
      (case (:type (ex-data e))
        :invalid-params (r/status 400)
        (throw e)))))

(defroutes handler
  (POST "/" _ create-patient!)
  (GET "/" _ list-patients))
