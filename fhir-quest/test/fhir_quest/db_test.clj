(ns fhir-quest.db-test
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [fhir-quest.db :as db]))

(def ^:private test-db-file (-> "java.io.tmpdir"
                                (System/getProperty)
                                (io/file "test.db")
                                (.getCanonicalPath)))

(def ^:private test-db-dsn (str "jdbc:sqlite:" test-db-file))

(use-fixtures :each
  (fn [f]
    (f)
    (io/delete-file test-db-file true)))

(deftest sqlite-dsn-test
  (testing "with db in the same dir"
    (is (= (db/sqlite-dsn "some.db") "jdbc:sqlite:some.db")))

  (testing "with db in a different dir"
    (is (= (db/sqlite-dsn "other/other.db") "jdbc:sqlite:other/other.db"))))

(deftest migrate-test
  (testing "database migration side-effects"
    (db/migrate! test-db-dsn)
    (jdbc/with-db-connection [db-conn {:connection-uri test-db-dsn}]
      (let [tables (-> db-conn
                       (jdbc/query "SELECT name FROM sqlite_master WHERE type='table'"
                                   {:row-fn #(get % :name)})
                       (set))]
        (is (contains? tables "encounter"))
        (is (contains? tables "patient"))
        (is (contains? tables "aggregation"))))))

(deftest spec-test
  (testing "with valid database file"
    (jdbc/with-db-connection [db-conn (db/spec test-db-file)]
      (is (jdbc/query db-conn "SELECT 1")))))
