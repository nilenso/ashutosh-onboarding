(ns fhir-quest.fhir-test
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [fhir-quest.factory :as factory]
            [fhir-quest.fhir :as fhir]
            [fhir-quest.fixtures :as fixtures]))

(defn- gen-fhir-bundle-json [resources]
  (json/generate-string {:resourceType "Bundle"
                         :entry (map #(do {:resource %}) resources)}))

(deftest read-bundles-test
  (testing "with empty directory"
    (fixtures/with-tmp-dir #(is (= '() (fhir/read-bundles %)))))

  (testing "with non-empty directory"
    (fixtures/with-tmp-dir
      (fn [tmp-dir]
        (spit (io/file tmp-dir "1.json") (gen-fhir-bundle-json (range 5)))
        (spit (io/file tmp-dir "2.not-json") "abcd")
        (spit (io/file tmp-dir "3.json") (gen-fhir-bundle-json (range 5 10)))
        (is (= (range 10)
               (fhir/read-bundles tmp-dir)))))))

(deftest resource-type-test
  (testing "with Patient resource type"
    (is (= "Patient" (fhir/resource-type {:resourceType "Patient"}))))

  (testing "with Encounter resource type"
    (is (= "Encounter" (fhir/resource-type {:resourceType "Encounter"})))))

(deftest encounter-subject-id-test
  (testing "with subject reference"
    (is (= "test-subject-id"
           (fhir/encounter-subject-id
            (factory/encounter
             {:subject {:reference "urn:uuid:test-subject-id"}})))))

  (testing "without subject reference"
    (is (= nil
           (fhir/encounter-subject-id
            (factory/encounter {:subject nil}))))))

(deftest encounter-duration-ms-test
  (testing "without start and end timestamps"
    (is (= nil
           (fhir/encounter-duration-ms
            (factory/encounter {:period nil})))))

  (testing "without start timestamp"
    (is (= nil
           (fhir/encounter-duration-ms
            (factory/encounter {:period {:start nil
                                         :end "2023-09-28T13:43:01+05:30"}})))))

  (testing "without end timestamp"
    (is (= nil
           (fhir/encounter-duration-ms
            (factory/encounter {:period {:start "2023-09-28T13:43:01+05:30"
                                         :end nil}})))))

  (testing "with start and end timestamps"
    (is (= 60000
           (fhir/encounter-duration-ms
            (factory/encounter {:period {:start "2023-09-28T13:43:00+05:30"
                                         :end "2023-09-28T13:44:00+05:30"}}))))))

(deftest patient-birth-date-test
  (testing "without birthDate attribute"
    (is (= nil
           (fhir/patient-birth-date (factory/patient {:birthDate nil})))))

  (testing "with birthDate attribute"
    (is (= "2023-09-28"
           (fhir/patient-birth-date
            (factory/patient {:birthDate "2023-09-28"}))))))

(deftest patient-language-test
  (testing "without language attribute"
    (is (= nil (fhir/patient-language (factory/patient {:communication nil})
                                      "test-system"))))

  (testing "without a value corresponding to the requested system"
    (is (= nil
           (fhir/patient-language (factory/patient
                                   {:communication [{:language {:coding [{:system "test-system"
                                                                          :code "en-IN"}]}}]})
                                  "another-system"))))

  (testing "with a value corresponding to the requested system"
    (is (= "en-IN"
           (fhir/patient-language (factory/patient
                                   {:communication [{:language {:coding [{:system "another-system"
                                                                          :code "English"}
                                                                         {:system "test-system"
                                                                          :code "en-IN"}]}}]})
                                  "test-system")))))

(deftest patient-marital-status-test
  (testing "without marital status"
    (is (= nil (fhir/patient-marital-status
                (factory/patient {:maritalStatus nil})
                "test-system"))))

  (testing "without a value corresponding to the requested system"
    (is (= nil (fhir/patient-marital-status
                (factory/patient
                 {:maritalStatus {:coding [{:system "test-system"
                                            :code "unknown"}]}})
                "another-system"))))

  (testing "with a value corresponding to the requested system"
    (is (= "unknown" (fhir/patient-marital-status
                      (factory/patient
                       {:maritalStatus {:coding [{:system "another-system"
                                                  :code "U"}
                                                 {:system "test-system"
                                                  :code "unknown"}]}})
                      "test-system")))))


