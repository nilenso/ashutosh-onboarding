(ns clinic.test-utils
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clinic.config :as config]
            [mount.core :as mount]))

(defn load-config-fixture [f]
  (mount/start #'config/config)
  (f)
  (mount/stop))

(defn expunge-fhir-data! [server-url]
  (-> server-url
      (str "/$expunge")
      (http/post {:headers {"Content-Type" "application/fhir+json"}
                  :body (json/generate-string {:resourceType "Parameters"
                                               :parameter [{:name "expungeEverything"
                                                            :valueBoolean true}]})
                  :throw-exceptions false})))

(defn expunge-fhir-data-fixture [f]
  (-> :fhir-server-base-url
      (config/get-value)
      (expunge-fhir-data!))
  (f))
