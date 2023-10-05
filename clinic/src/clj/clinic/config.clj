(ns clinic.config
  (:refer-clojure :exclude [read])
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [mount.core :refer [defstate]]))

(defstate ^:private config
  :start (-> "config.edn"
             (io/resource)
             (aero/read-config))
  :stop nil)

(defn read [key]
  (get config key))
