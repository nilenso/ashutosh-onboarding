(ns clinic.routes.core
  (:require [clinic.config :as config]
            [clinic.routes.patient :as patient]
            [clojure.tools.logging :as log]
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
           (log/warn t "encountered unexpected error for request:" request)
           (r/status 500)))))

(def handler
  "The default API route handler."
  (-> routes
      (config/wrap)
      (wrap-json-body {:keywords? true})
      (wrap-json-response)
      (wrap-exception-handler)))
