(ns fhir-quest.core
  (:require [cli-matic.core :as cm]
            [fhir-quest.db :as db]))

(defn- sqlite-dsn [db-name]
  (str "jdbc:sqlite:" db-name))

(defn- init [args]
  (db/migrate! (sqlite-dsn (get args :sqlite-db))))

(defn- with-init [f]
  (fn [args]
    (init args)
    (f args)))

(defn ingest-cmd [args]
  (prn args))

(defn -main [& args]
  (cm/run-cmd args {:app {:command "fhir-quest"
                          :description "Simple analysis queries on (generated) medical data conforming to FHIR R4 data specifications."}
                    :global-opts [{:option "sqlite-db"
                                   :short "db"
                                   :as "File path for an SQLite database for storing and accessing aggregates."
                                   :type :string
                                   :default "fhir-quest.db"}]
                    :commands [{:command "ingest"
                                :short "i"
                                :description ["Process the given dataset and persist its aggregates."]
                                :opts [{:option "data-dir"
                                        :short "d"
                                        :as "File path for a directory containing FHIR JSON bundles."
                                        :type :string
                                        :default "synthea/fhir"}]
                                :runs (with-init ingest-cmd)}]}))
