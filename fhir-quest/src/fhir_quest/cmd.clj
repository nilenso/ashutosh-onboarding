(ns fhir-quest.cmd
  (:require [clojure.java.jdbc :as jdbc]
            [fhir-quest.fhir :as fhir]
            [fhir-quest.ingester :as ingester]))

(defn ingest
  "Runs ingesters on FHIR resources present in directory `input-dir`. Ingesters
   need a `clojure.java.jdbc` compliant databse specification `db-spec` to
   persist proccessed data."
  [db-spec input-dir]
  (jdbc/with-db-transaction [db-conn db-spec]
    (doseq [entry (fhir/read-bundles input-dir)]
      (ingester/ingest-fhir-resource! db-conn entry))))
