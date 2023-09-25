(ns fhir-quest.ingester
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]))

(defn- ingest-encounter! [db-conn e]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO
                       encounter (id, subject_id, duration_ms)
                       VALUES (?, ?, ?)"
                  (fhir/id e)
                  (fhir/encounter-subject-id e)
                  (fhir/encounter-duration-ms e)]))

(defn- ingest-patient! [db-conn p]
  (jdbc/execute! db-conn
                 ["INSERT OR REPLACE INTO
                     patient (id, birth_date, language, marital_status)
                     VALUES (?, ?, ?, ?)"
                  (fhir/id p)
                  (fhir/patient-birth-date p)
                  (fhir/patient-language p "urn:ietf:bcp:47")
                  (fhir/patient-marital-status p "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus")]))

(def ^:private ingesters {"Encounter" ingest-encounter!
                          "Patient" ingest-patient!})

(defn ingest-fhir-resource!
  "Runs ingesters on a FHIR resource based on its `resourceType`."
  [db-conn entry]
  (when-let [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
