(ns fhir-quest.db
  (:import org.flywaydb.core.Flyway))

(defn sqlite-dsn
  "Returns a JDBC compliant data-source name for SQLite database `db`."
  [db]
  (str "jdbc:sqlite:" db))

(defn migrate!
  "Applies database migrations from `resources/db-migrations` directory to the
   database specified by data-source name `dsn`."
  [dsn]
  (-> (Flyway/configure)
      (.dataSource dsn nil nil)
      (.locations (into-array String ["classpath:db-migrations"]))
      (.loggers (into-array String []))
      (.load)
      (.migrate)))

(defn spec
  "Returns a `clojure.java.jdbc` compliant database specification for SQLite
   database `db`."
  [db]
  {:connection-uri (sqlite-dsn db)})
