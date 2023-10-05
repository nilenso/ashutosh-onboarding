(ns clj.clinic.config-test
  (:require [clojure.test :refer [deftest is]]
            [aero.core :as aero]
            [clinic.config :as config]
            [mount.core :as mount]))

(deftest read-test
  (with-redefs [aero/read-config (constantly {:test-key "test-val"})]
    (mount/start #'config/config)
    (is (= "test-val" (config/read :test-key)))))
