(ns clinic.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [mount.core :refer [defstate]]))

(defstate ^:private config
  :start (-> "config.edn"
             (io/resource)
             (aero/read-config))
  :stop nil)

(defn get-value
  "Returns the configuration value corresponding to the given `key` in
   `resources/config.edn`."
  [key]
  (get config key))

(defn wrap
  "Ring middleware to add `:config` key to incoming requests."
  [next-handler]
  (fn [request]
    (-> {:config config}
        (into request)
        (next-handler))))
