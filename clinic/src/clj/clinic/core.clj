(ns clinic.core
  (:require [clinic.config :as config]
            [mount.core :as mount]))

(defn -main []
  (mount/start)
  (prn (config/read :fhir-server-base-url) (config/read :http-port)))
