(ns fhir-quest.ingester
  (:require [fhir-quest.fhir :as fhir]
            [fhir-quest.repository :as repo]))

(defn- ingest-encounter! [db-conn e]
  (repo/save-encounter! db-conn
                        (fhir/id e)
                        (fhir/encounter-subject-id e)
                        (fhir/encounter-duration-ms e)))

(defn- ingest-patient! [db-conn p]
  (repo/save-patient! db-conn
                      (fhir/id p)
                      (fhir/patient-birth-date p)
                      (fhir/patient-language p "urn:ietf:bcp:47")
                      (fhir/patient-marital-status p "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus")))

(def ^:private ingesters {"Encounter" ingest-encounter!
                          "Patient" ingest-patient!})

(defn ingest-fhir-resource!
  "Runs ingesters on a FHIR resource based on its `resourceType`."
  [db-conn entry]
  (when-let [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
