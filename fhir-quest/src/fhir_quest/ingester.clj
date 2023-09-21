(ns fhir-quest.ingester
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]))

(defn- encounter-duration-avg []
  (fn [db-conn e]
    (let [[old-data] (jdbc/query db-conn
                                 "SELECT * FROM encounter_duration_avg LIMIT 1")
          old-avg (get old-data :duration_ms 0)
          old-count (get old-data :encounter_count 0)
          new-duration (fhir/encounter-duration-ms e)
          new-count (inc old-count)
          new-avg (-> old-avg
                      (* old-count)
                      (+ new-duration)
                      (/ new-count)
                      (int))]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO encounter_duration_avg VALUES (?, ?, ?)"
                      0
                      new-avg
                      new-count]))))

(defn- encounter-duration-avg-by-subject []
  (fn [db-conn e]
    (let [subject-id (fhir/encounter-subject-id e)

          [old-data] (jdbc/query db-conn
                                 ["SELECT * FROM subject_encounter_duration_avg WHERE subject_id = ? LIMIT 1"
                                  subject-id])
          old-avg (get old-data :duration_ms 0)
          old-count (get old-data :encounter_count 0)
          new-duration (fhir/encounter-duration-ms e)
          new-count (inc old-count)
          new-avg (-> old-avg
                      (* old-count)
                      (+ new-duration)
                      (/ new-count)
                      (int))]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO subject_encounter_duration_avg VALUES (?, ?, ?)"
                      subject-id
                      new-avg
                      new-count]))))

(defn- patient-age-group []
  (let [age-classifier (fhir/patient-age-classifier {:infant [0 1]
                                                     :child [2 12]
                                                     :adolescent [13 17]
                                                     :adult [18 64]
                                                     :older-adult [65 ##Inf]})]
    (fn [db-conn p]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO patient_age_group VALUES (?, ?)"
                      (fhir/id p)
                      (age-classifier p)])
      nil)))

(defn- patient-language []
  (let [extractor (fhir/patient-language-extractor "urn:ietf:bcp:47")]
    (fn [db-conn p]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO patient_language VALUES (?, ?)"
                      (fhir/id p)
                      (extractor p)]))))

(defn- patient-marital-status []
  (let [extractor (-> "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus"
                      (fhir/patient-marital-status-extractor))]
    (fn [db-conn p]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO patient_marital_status VALUES (?, ?)"
                      (fhir/id p)
                      (extractor p)]))))

(def ^:private ingesters {"Encounter" (juxt (encounter-duration-avg)
                                            (encounter-duration-avg-by-subject))
                          "Patient" (juxt (patient-age-group)
                                          (patient-language)
                                          (patient-marital-status))})

(defn ingest!
  "Runs ingesters on a FHIR resource based on its `resourceType`."
  [db-conn entry]
  (when-let [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
