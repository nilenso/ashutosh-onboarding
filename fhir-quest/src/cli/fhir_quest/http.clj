(ns fhir-quest.http
  (:require [cheshire.core :as json]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found resources]]
            [fhir-quest.service :as svc]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as r]))

(defn- list-aggregations [{db-spec :db-spec}]
  (-> db-spec
      (svc/list-aggregations)
      (json/encode)
      (r/response)
      (r/header "Content-Type" "application/json")))

(defn- get-aggregation-chart [{db-spec :db-spec
                               {id :id} :params}]
  (-> db-spec
      (svc/get-aggregation-chart id)
      (json/encode)
      (r/response)
      (r/header "Content-Type" "application/json")))

(defroutes routes
  (GET "/api/v1/aggregation" _ list-aggregations)
  (GET "/api/v1/aggregation/:id/chart" _ get-aggregation-chart)
  (GET "/" _ (r/resource-response "index.html" {:root "public"}))
  (resources "/")
  (not-found (-> "Page not found"
                 (r/response)
                 (r/status 404))))

(defn- wrap-db-spec [db-spec next-handler]
  (fn [request]
    (->> {:db-spec db-spec}
         (into request)
         (next-handler))))

(defn start-server
  "Starts a Jetty server on the given `port` and passes the given `db-spec` to
   request handlers with request parameters. If `:join-thread` is specified,
   joins the current thread with server's connection listen thread. The default
   is `false`."
  [db-spec port & {join-thread :join-thread
                   :or {join-thread false}}]
  (jetty/run-jetty (wrap-db-spec db-spec #'routes)
                   {:port port :join? join-thread}))
