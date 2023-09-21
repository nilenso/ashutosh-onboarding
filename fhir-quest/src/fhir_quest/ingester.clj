(ns fhir-quest.ingester
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]))

(defn- patient-age-group []
  (let [age-classifier (fhir/patient-age-classifier {:infant [0 1]
                                                     :child [2 12]
                                                     :adolescent [13 17]
                                                     :adult [18 64]
                                                     :older-adult [65 100]})]
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

(def ^:private ingesters {"Patient" (juxt (patient-age-group)
                                          (patient-language)
                                          (patient-marital-status))})

(defn ingest!
  "Runs ingesters on a FHIR resource based on its `resourceType`."
  [db-conn entry]
  (when-let [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
