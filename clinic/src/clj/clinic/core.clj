(ns clinic.core
  (:require [clinic.config :as config]
            [clinic.routes.core :as routes]
            [mount.core :as mount]
            [ring.adapter.jetty :as jetty]))

(defonce server (atom nil))

(defn- start
  ([] (start false))
  ([join-thread]
   (mount/start)
   (reset! server
           (jetty/run-jetty (config/wrap #'routes/handler)
                            {:port (config/get-value :http-port)
                             :join? join-thread}))))

(defn -main []
  (start true))

;; REPL helpers
(comment (start))
(comment
  (when @server
    (.stop @server))
  (mount/stop))
