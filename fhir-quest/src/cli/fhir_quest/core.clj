(ns fhir-quest.core
  (:require [cli-matic.core :as cm]
            [fhir-quest.cmd :as cmd]
            [fhir-quest.db :as db]))

(defn- init! [args]
  (-> args
      (get :sqlite-db)
      (db/sqlite-dsn)
      (db/migrate!))
  nil)

(defn- wrap-init [f]
  (fn [args]
    (init! args)
    (f args)))

(defn- ingest! [{db-path :sqlite-db
                input-dir :data-dir}]
  (cmd/ingest! (db/spec db-path) input-dir))

(defn- serve! [{db-path :sqlite-db
               http-port :port}]
  (cmd/serve! (db/spec db-path)
             http-port))

(defn -main [& args]
  (cm/run-cmd args {:app {:command "fhir-quest"
                          :description "Simple analysis queries on (generated) medical data conforming to FHIR R4 data specifications."}
                    :global-opts [{:option "sqlite-db"
                                   :short "d"
                                   :as "File path for an SQLite database for storing and accessing aggregates."
                                   :type :string
                                   :default "./fhir-quest.db"}]
                    :commands [{:command "ingest"
                                :short "i"
                                :description ["Process the given dataset and persist its aggregates."]
                                :opts [{:option "data-dir"
                                        :short "i"
                                        :as "File path for a directory containing FHIR JSON bundles."
                                        :type :string
                                        :default "./synthea/fhir"}]
                                :runs (wrap-init ingest!)}
                               {:command "serve"
                                :short "s"
                                :description ["Serve HTTP server for UI and data."]
                                :opts [{:option "port"
                                        :short "p"
                                        :as "Listen port for incoming HTTP connections."
                                        :type :int
                                        :default 8080}]
                                :runs (wrap-init serve!)}]}))
