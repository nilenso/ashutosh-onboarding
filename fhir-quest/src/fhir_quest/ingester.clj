(ns fhir-quest.ingester
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]))

(defn patient-age-group [age-groups]
  (let [age-classifier (fhir/patient-by-age-classifier age-groups)]
    (fn [db-conn p]
      (jdbc/execute! db-conn
                     ["INSERT OR REPLACE INTO patient_age_group VALUES (?, ?)"
                      (fhir/id p)
                      (age-classifier p)])
      nil)))

(def ingesters {"Patient" [(patient-age-group {:infant [0 1]
                                               :child [2 12]
                                               :adolescent [13 17]
                                               :adult [18 64]
                                               :older-adult [65 100]})]})

(defn ingest! [db-conn entry]
  (doseq [ingester! (get ingesters (fhir/resource-type entry))]
    (ingester! db-conn entry)))
