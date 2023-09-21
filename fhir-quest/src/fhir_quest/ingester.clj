(ns fhir-quest.ingester
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]))

(defn- ingest-encounter-duration-avg! [db-conn e]
  (let [[old-data] (jdbc/query db-conn
                               "SELECT duration_ms, encounter_count FROM encounter_duration_avg LIMIT 1")
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
                   ["INSERT OR REPLACE INTO encounter_duration_avg (id, duration_ms, encounter_count) VALUES (?, ?, ?)"
                    0
                    new-avg
                    new-count])))

(defn- ingest-encounter-duration-avg-by-subject! [db-conn e]
  (let [subject-id (fhir/encounter-subject-id e)
        [old-data] (jdbc/query db-conn
                               ["SELECT duration_ms, encounter_count FROM subject_encounter_duration_avg WHERE subject_id = ? LIMIT 1"
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
                   ["INSERT OR REPLACE INTO subject_encounter_duration_avg (subject_id, duration_ms, encounter_count) VALUES (?, ?, ?)"
                    subject-id
                    new-avg
                    new-count])))

(defn- ingest-patient-age! [db-conn p]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO patient_age (patient_id, age) VALUES (?, ?)"
                  (fhir/id p)
                  (fhir/patient-age p)]))

(defn- ingest-patient-language! [db-conn p]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO patient_language (patient_id, language) VALUES (?, ?)"
                  (fhir/id p)
                  (fhir/patient-language p "urn:ietf:bcp:47")]))

(defn- ingest-patient-marital-status! [db-conn p]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO patient_marital_status (patient_id, status) VALUES (?, ?)"
                  (fhir/id p)
                  (fhir/patient-marital-status p "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus")]))

(def ^:private ingesters {"Encounter" (juxt ingest-encounter-duration-avg!
                                            ingest-encounter-duration-avg-by-subject!)
                          "Patient" (juxt ingest-patient-age!
                                          ingest-patient-language!
                                          ingest-patient-marital-status!)})

(defn ingest!
  "Runs ingesters on a FHIR resource based on its `resourceType`."
  [db-conn entry]
  (when-let [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
