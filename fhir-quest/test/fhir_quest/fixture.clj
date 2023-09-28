(ns fhir-quest.fixture
  (:require [clojure.java.io :as io]
            [fhir-quest.db :as db]
            [clojure.java.jdbc :as jdbc]))

(defn with-tmp-dir [f]
  (let [tmp-dir (-> "java.io.tmpdir"
                    (System/getProperty)
                    (io/file (str (System/currentTimeMillis))))]
    (.mkdirs tmp-dir)
    (f tmp-dir)
    (run! io/delete-file (reverse (file-seq tmp-dir)))))

(defn with-tmp-file [file-name f]
  (with-tmp-dir #(f (io/file % file-name))))

(defn with-tmp-db [f]
  (with-tmp-file (str (System/currentTimeMillis) ".db")
    (fn [db-file]
      (let [dsn (str "jdbc:sqlite:" db-file)]
        (db/migrate! dsn)
        (jdbc/with-db-connection [db-conn {:connection-uri dsn}]
          ;; remove everything from tables as schema migration may pre-populate
          ;; data.
          (doseq [table (jdbc/query db-conn
                                    "SELECT name FROM sqlite_master WHERE type='table'"
                                    {:row-fn #(get % :name)})]
            (jdbc/execute! db-conn (str "DELETE FROM " table)))
          (f db-conn))))))
