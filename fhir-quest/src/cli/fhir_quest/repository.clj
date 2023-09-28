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

(defn update-aggregation-data! [db-conn agg-id data]
  (jdbc/execute! db-conn
                 ["UPDATE aggregation SET data_json = ? WHERE id = ?"
                  (json/encode-smile data)
                  agg-id]))

(defn list-encounters [db-conn]
  (jdbc/query db-conn "SELECT * FROM encounter"))

(defn list-patients [db-conn]
  (jdbc/query db-conn "SELECT * FROM patient"))

(defn save-encounter! [db-conn id subject-id duration-ms]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO
                          encounter (id, subject_id, duration_ms)
                          VALUES (?, ?, ?)"
                  id
                  subject-id
                  duration-ms]))

(defn save-patient! [db-conn id birth-date language marital-status]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO
                        patient (id, birth_date, language, marital_status)
                        VALUES (?, ?, ?, ?)"
                  id
                  birth-date
                  language
                  marital-status]))
