(ns fhir-quest.db-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [fhir-quest.db :as db]
            [fhir-quest.fixtures :as fixtures]))

(deftest sqlite-dsn-test
  (testing "with db in the same dir"
    (is (= (db/sqlite-dsn "some.db") "jdbc:sqlite:some.db")))

  (testing "with db in a different dir"
    (is (= (db/sqlite-dsn "other/other.db") "jdbc:sqlite:other/other.db"))))

(deftest migrate-test
  (testing "database migration side-effects"
    (fixtures/with-tmp-file "migrate-test.db"
      (fn [db-file]
        (let [db-dsn (str "jdbc:sqlite:" (.getCanonicalPath db-file))]
          (db/migrate! db-dsn)
          (jdbc/with-db-connection [db-conn {:connection-uri db-dsn}]
            (let [tables (-> db-conn
                             (jdbc/query "SELECT name FROM sqlite_master WHERE type='table'"
                                         {:row-fn #(get % :name)})
                             (set))]
              (is (contains? tables "encounter"))
              (is (contains? tables "patient"))
              (is (contains? tables "aggregation")))))))))

(deftest spec-test
  (testing "with valid database file"
    (fixtures/with-tmp-file "spec-test.db"
      (fn [db-file]
        (jdbc/with-db-connection [db-conn (db/spec db-file)]
          (is (jdbc/query db-conn "SELECT 1")))))))
