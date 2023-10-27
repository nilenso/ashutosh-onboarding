(ns clinic.test-utils
  (:require [cheshire.core :as json]
            [clinic.config :as config]
            [clj-http.client :as http]
            [mount.core :as mount]))

(defn load-config-fixture [f]
  (mount/start #'config/config)
  (f)
  (mount/stop))

(defn expunge-fhir-data! []
  (-> (config/get-value :fhir-server-base-url)
      (str "/$expunge")
      (http/post {:headers {"Content-Type" "application/fhir+json"}
                  :body (json/generate-string {:resourceType "Parameters"
                                               :parameter [{:name "expungeEverything"
                                                            :valueBoolean true}]})})))

(defn create-fhir-patient! [patient]
  (-> (config/get-value :fhir-server-base-url)
      (str "/Patient")
      (http/post {:headers {"Content-Type" "application/fhir+json"}
                  :body (json/generate-string patient)})
      (update :body json/parse-string true)))

(defn digits-equal?
  "Checks if digits in the given strings are in the same order and equal,
   ignoring all other characters.

       (digits-equal? \"a1b2c3\" \"123abc\") ;=> true
       (digits-equal? \"a3b2c1\" \"123abc\") ;=> false
   "
  [this other]
  (= (re-seq #"\d" this)
     (re-seq #"\d" other)))

(defn mock-fn []
  (let [call-args (atom [])
        response-fn (atom (constantly nil))]
    [call-args
     response-fn
     (fn [& args]
       (reset! call-args (vec args))
       (apply @response-fn args))]))

(defmacro catch-thrown-data [& body]
  `(try ~@body
        (catch clojure.lang.ExceptionInfo e# (ex-data e#))))
