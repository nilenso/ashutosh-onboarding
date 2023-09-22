(ns fhir-quest.http
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as r]))

(defn- list-queries [{db-spec :db-spec}]
  (-> (jdbc/query db-spec "SELECT id, description FROM query")
      (json/encode)
      (r/response)
      (r/header "Content-Type" "application/json")))

(defn- get-query [{db-spec :db-spec
                   {id :id} :params}]
  (-> (jdbc/query db-spec
                  ["SELECT chart_type AS type, data_json
                      FROM query WHERE id = ? LIMIT 1"
                   id])
      (first)
      (#(do {:type (get % :type)
             :data (-> (get % :data_json)
                       (json/parse-smile))}))
      (json/encode)
      (r/response)
      (r/header "Content-Type" "application/json")))

(defroutes routes
  (GET "/api/v1/query" _ list-queries)
  (GET "/api/v1/query/:id/chart" _ get-query)
  (not-found (-> "Page not found"
                 (r/response)
                 (r/status 404))))

(defn- wrap-db-spec [db-spec next-handler]
  (fn [request]
    (->> {:db-spec db-spec}
         (into request)
         (next-handler))))

(defn start-server [db-spec port & {join-thread :join-thread
                                    :or {join-thread false}}]
  (jetty/run-jetty (wrap-db-spec db-spec #'routes)
                   {:port port :join? join-thread}))
