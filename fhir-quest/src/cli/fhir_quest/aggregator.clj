(ns fhir-quest.aggregator
  (:require [clojure.string :as string]
            [fhir-quest.repository :as repo]
            [fhir-quest.utils :as utils])
  (:import java.util.concurrent.TimeUnit))

(defn- aggregate-encounter-duration-avg! [db-conn]
  (let [avg-duration-mins
        (->> db-conn
             (repo/list-encounters)
             (reduce #(conj %1 (get %2 :duration_ms)) [])
             (utils/average)
             (.toMinutes TimeUnit/MILLISECONDS))]
    (repo/update-aggregation-data db-conn
                                  "encounter-duration-avg"
                                  [{:label "Average (minutes)"
                                    :value avg-duration-mins}])))

(defn- aggregate-patient-encounter-duration-groups! [db-conn]
  (->> db-conn
       (repo/list-encounters)
       (reduce #(assoc %1
                       (get %2 :subject_id)
                       (conj (get %1 :subject_id [])
                             (get %2 :duration_ms)))
               {})
       (reduce-kv #(conj %1 (utils/average %3)) [])
       (utils/count-by (utils/classifier {"0:< 15 mins" [0 900000]
                                          "1:15-30 mins" [900001 1800000]
                                          "2:30-45 mins" [1800001 2700000]
                                          "3:45-60 mins" [2700001 3600000]
                                          "4:1-2 hours" [3600001 7200000]
                                          "5:2-4 hours" [7200001 14400000]
                                          "6:4-8 hours" [14400001 28800000]
                                          "7:> 8 hours" [28800001 ##Inf]}
                                         "8:Unknown"))
       (into (sorted-map))
       (map (fn [[k v]]
              {:label (last (string/split k #":"))
               :value v}))
       (repo/update-aggregation-data db-conn "patient-encounter-duration-groups")))

(defn- aggregate-patient-age-groups! [db-conn]
  (->> db-conn
       (repo/list-patients)
       (map #(utils/months-since (get % :birth_date)))
       (utils/count-by (utils/classifier {"0:Neonates" [0 1]
                                          "1:Infants" [2 12]
                                          "2:Children" [13 144]
                                          "3:Adolescents" [145 204]
                                          "4:Adults" [205 780]
                                          "5:Older Adults" [781 ##Inf]}
                                         "6:Unknown"))
       (into (sorted-map))
       (map (fn [[k v]]
              {:label (last (string/split k #":"))
               :value v}))
       (repo/update-aggregation-data db-conn "patient-age-group")))

(defn- aggregate-patient-language-groups! [db-conn]
  (->> db-conn
       (repo/list-patients)
       (utils/count-by #(get % :language))
       (map (fn [[k v]] {:label k :value v}))
       (repo/update-aggregation-data db-conn "patient-language")))

(defn- aggregate-patient-marital-status-groups! [db-conn]
  (->> db-conn
       (repo/list-patients)
       (utils/count-by #(get % :marital_status))
       (map (fn [[k v]]
              {:label (case k
                        "D" "Divorced"
                        "M" "Married"
                        "S" "Single"
                        "W" "Windowed")
               :value v}))
       (repo/update-aggregation-data db-conn "patient-marital-status")))

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
