(ns fhir-quest.db-test
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest is testing]]
            [fhir-quest.db :as db]
            [fhir-quest.fixture :as fixture]))

(deftest sqlite-dsn-test
  (testing "with db in the same dir"
    (is (= (db/sqlite-dsn "some.db") "jdbc:sqlite:some.db")))

  (testing "with db in a different dir"
    (is (= (db/sqlite-dsn "other/other.db") "jdbc:sqlite:other/other.db"))))

(deftest spec-test
  (testing "with valid database file"
    (fixture/with-tmp-file "spec-test.db"
      (fn [db-file]
        (jdbc/with-db-connection [db-conn (db/spec db-file)]
          (is (jdbc/query db-conn "SELECT 1")))))))
