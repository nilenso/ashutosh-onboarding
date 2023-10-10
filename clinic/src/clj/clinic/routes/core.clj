(ns clinic.routes.core
  (:require [clinic.config :as config]
            [clinic.routes.patient :as patient]
            [clojure.stacktrace :as stacktrace]
            [compojure.core :refer [context defroutes]]
            [compojure.route :refer [not-found]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.util.response :as r]))

(defroutes ^:private routes
  (context "/api/v1" _
    (context "/patients" _ patient/handler))
  (not-found (r/status 404)))

(defn- wrap-exception-handler [next-handler]
  (fn exception-handler [request]
    (try (next-handler request)
         (catch Throwable t
           (println "encountered unexpected error for request:" request)
           (stacktrace/print-stack-trace t)
           (r/status 500)))))

(def handler
  "The default API route handler."
  (-> routes
      (config/wrap)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)
      (wrap-exception-handler)))
