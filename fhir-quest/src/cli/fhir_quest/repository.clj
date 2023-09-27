(ns fhir-quest.repository
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]))

(defn- aggregation--dao->domain [{:keys [id description chart_type data_json]}]
  {:id id
   :description description
   :chart_type chart_type
   :data (-> data_json
             (json/parse-smile true)
             (vec))})

(defn list-aggregations [db-conn offset limit]
  (jdbc/query db-conn
              ["SELECT * FROM aggregation LIMIT ? OFFSET ?" limit offset]
              {:row-fn aggregation--dao->domain}))

(defn get-aggregation [db-conn id]
  (->  db-conn
       (jdbc/query
        ["SELECT * FROM aggregation WHERE id = ? LIMIT 1" id]
        {:row-fn aggregation--dao->domain})
       (first)))

(defn update-aggregation-data [db-conn agg-id data]
  (jdbc/execute! db-conn
                 ["UPDATE aggregation SET data_json = ? WHERE id = ?"
                  (json/encode-smile data)
                  agg-id]))

(defn list-encounters [db-conn]
  (jdbc/query db-conn "SELECT * FROM encounter"))

(defn list-patients [db-conn]
  (jdbc/query db-conn "SELECT * FROM patient"))
