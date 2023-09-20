(ns fhir-quest.db
  (:import org.flywaydb.core.Flyway))

(defn migrate! [dsn]
  (-> (Flyway/configure)
      (.dataSource dsn nil nil)
      (.locations (into-array String ["classpath:db-migrations"]))
      (.loggers (into-array String []))
      (.load)
      (.migrate)))
