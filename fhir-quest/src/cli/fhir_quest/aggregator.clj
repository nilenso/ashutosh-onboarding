(ns fhir-quest.aggregator
  (:require [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]
            [fhir-quest.utils :as utils]))

(defn- update-query-data [db-conn query-id data]
  (jdbc/execute! db-conn
                 ["UPDATE query SET data_json = ? WHERE id = ?"
                  (json/encode-smile data)
                  query-id]))

(defn- aggregate-encounter-duration-avg! [db-conn]
  (let [avg-duration-ms (-> db-conn
                            (jdbc/query "SELECT AVG(duration_ms) AS avg_duration_ms
                                           FROM encounter")
                            (first)
                            (get :avg_duration_ms)
                            (Math/round))]
    (update-query-data db-conn
                       "encounter-duration-avg"
                       [{:label "Average (ms)"
                         :value avg-duration-ms}])))

(defn- aggregate-patient-encounter-duration-groups! [db-conn]
  (->> (jdbc/query db-conn
                   "SELECT subject_id, AVG(duration_ms) AS avg_duration_ms
                      FROM encounter GROUP BY subject_id"
                   {:row-fn #(-> %
                                 (get :avg_duration_ms)
                                 (Math/round))})
       (map (utils/classifier {"Very Short" [0 1800000]
                               "Short" [1801000 7200000]
                               "Medium" [7201000 14400000]
                               "Long" [14401000 28800000]
                               "Very Long" [28801000 ##Inf]}
                              "Unknown"))
       (reduce utils/freq-counting {"Very Short" 0
                                    "Short" 0
                                    "Medium" 0
                                    "Long" 0
                                    "Very Long" 0}) ; init for preserving key order
       (map (fn [[k v]] {:label k :value v}))
       (update-query-data db-conn "patient-encounter-duration-groups")))

(defn- aggregate-patient-age-groups! [db-conn]
  (->> (jdbc/query db-conn
                   "SELECT birth_date FROM patient"
                   {:row-fn #(get % :birth_date)})
       (map utils/months-since)
       (map (utils/classifier {"Neonates" [0 1]
                               "Infants" [2 12]
                               "Children" [13 144]
                               "Adolescents" [145 204]
                               "Adults" [205 780]
                               "Older Adults" [781 ##Inf]}
                              "Unknown"))
       (reduce utils/freq-counting {"Neonates" 0
                                    "Infants" 0
                                    "Children" 0
                                    "Adolescents" 0
                                    "Adults" 0
                                    "Older Adults" 0}) ; init for preserving key order
       (map (fn [[k v]] {:label k :value v}))
       (update-query-data db-conn "patient-age-group")))

(defn- aggregate-patient-language-groups! [db-conn]
  (->> (jdbc/query db-conn
                   "SELECT language as label, COUNT(id) AS value
                      FROM patient GROUP BY language ORDER BY language")
       (update-query-data db-conn "patient-language")))

(defn- aggregate-patient-marital-status-groups! [db-conn]
  (->> (jdbc/query db-conn
                   "SELECT marital_status as label, COUNT(id) AS value
                      FROM patient GROUP BY marital_status ORDER BY marital_status")
       (update-query-data db-conn "patient-marital-status")))

(defn aggregate!
  "Runs aggregators on the ingested data and updates chart data for all queries
   in the database with updated aggregations."
  [db-conn]
  (doto db-conn
    (aggregate-encounter-duration-avg!)
    (aggregate-patient-encounter-duration-groups!)
    (aggregate-patient-age-groups!)
    (aggregate-patient-language-groups!)
    (aggregate-patient-marital-status-groups!)))
