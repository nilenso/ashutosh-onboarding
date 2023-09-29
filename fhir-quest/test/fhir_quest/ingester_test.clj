(ns fhir-quest.ingester-test
  (:require [clojure.test :refer [deftest is testing]]
            [fhir-quest.repository :as repo]
            [fhir-quest.factory :as factory]
            [fhir-quest.ingester :as ingester]))

(deftest ingest-test
  (testing "with Encounter resource"
    (let [call-args (atom nil)
          want-id (str (random-uuid))
          want-subject-ref (str (random-uuid))]
      (with-redefs [repo/save-encounter! (fn [_ & args]
                                           (reset! call-args (vec args)))]
        (ingester/ingest-fhir-resource! nil
                                        (factory/encounter {:id want-id
                                                            :subject {:reference want-subject-ref}
                                                            :period {:start "2000-01-01T00:00:00+05:30"
                                                                     :end "2000-01-01T00:01:00+05:30"}}))
        (is (= want-id (@call-args 0)))
        (is (= want-subject-ref (@call-args 1)))
        (is (= 60000 (@call-args 2))))))

  (testing "with Patient resource"
    (let [call-args (atom nil)
          want-id (str (random-uuid))
          want-birth-date "2000-02-02"
          want-language "en-IN"
          want-marital-status "S"]
      (with-redefs [repo/save-patient! (fn [_ & args]
                                         (reset! call-args (vec args)))]
        (ingester/ingest-fhir-resource!
         nil
         (factory/patient
          {:id want-id
           :birthDate want-birth-date
           :communication [{:language {:coding [{:system "urn:ietf:bcp:47"
                                                 :code want-language}]}}]
           :maritalStatus {:coding [{:system "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus"
                                     :code want-marital-status}]}}))
        (is (= want-id (@call-args 0)))
        (is (= want-birth-date (@call-args 1)))
        (is (= want-language (@call-args 2)))
        (is (= want-marital-status (@call-args 3)))))))
