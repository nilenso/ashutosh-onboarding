(ns clinic.config-test
  (:require [aero.core :as aero]
            [clinic.config :as config]
            [clojure.test :refer [deftest is]]
            [mount.core :as mount]))

(deftest get-value-test
  (with-redefs [aero/read-config (constantly {:test-key "test-val"})]
    (mount/start #'config/config)
    (is (= "test-val" (config/get-value :test-key)))))

(deftest wrap-test
  (let [request (atom {})
        next-handler (partial reset! request)
        test-config {:test-key "test-val"}]
    (with-redefs [aero/read-config (constantly test-config)]
      (mount/start #'config/config)
      ((config/wrap next-handler) {:method :get})
      (is (= test-config (@request :config))))))
