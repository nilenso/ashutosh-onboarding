(ns clinic.test-utils
  (:require [cheshire.core :as json]
            [clinic.config :as config]
            [clj-http.client :as http]
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

(defn digits-equal?
  "Checks if digits in the given strings are in the same order and equal,
   ignoring all other characters.

       (digits-equal? \"a1b2c3\" \"123abc\") ;=> true
       (digits-equal? \"a3b2c1\" \"123abc\") ;=> false
   "
  [this other]
  (= (re-seq #"\d" this)
     (re-seq #"\d" other)))
