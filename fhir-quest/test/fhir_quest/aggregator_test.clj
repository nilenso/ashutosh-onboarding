(ns fhir-quest.aggregator-test
  (:require [clojure.test :refer [deftest is]]
            [fhir-quest.aggregator :as aggregator]
            [fhir-quest.factory :as factory]
            [fhir-quest.repository :as repo]
            [java-time.api :as jt]))

(deftest aggregate-encounter-duration-avg-test
  (let [update-call-args (atom nil)]
    (with-redefs [repo/list-encounters (->> {:duration_ms 600000}
                                            (partial factory/encounter-dbo)
                                            (repeatedly 5)
                                            (vec)
                                            (constantly))
                  repo/update-aggregation-data! (fn [_ & args]
                                                  (reset! update-call-args (vec args)))]
      (aggregator/aggregate-encounter-duration-avg! nil)
      (is (= "encounter-duration-avg"  (get @update-call-args 0)))
      (is (= [{:label "Average (minutes)" :value 10}] (get @update-call-args 1))))))

(deftest aggregate-patient-encounter-duration-groups-test
  (let [dataset (-> []
                    (into (repeatedly 5
                                      #(factory/encounter-dbo {:subject_id "test-subject-1"
                                                               :duration_ms 600000})))
                    (into (repeatedly 5
                                      #(factory/encounter-dbo {:subject_id "test-subject-2"
                                                               :duration_ms 2700000}))))
        update-call-args (atom nil)]
    (with-redefs [repo/list-encounters (constantly dataset)
                  repo/update-aggregation-data! (fn [_ & args]
                                                  (reset! update-call-args (vec args)))]
      (aggregator/aggregate-patient-encounter-duration-groups! nil)
      (is (= "patient-encounter-duration-groups" (get @update-call-args 0)))
      (is (= [{:label "< 15 mins", :value 1} {:label "30-45 mins", :value 1}]
             (get @update-call-args 1))))))

(deftest aggregate-patient-age-groups-test
  (jt/with-clock (jt/mock-clock (jt/instant "2000-01-01T00:00:00+05:30"))
    (let [dataset (-> []
                      (into (repeatedly 5 #(factory/patient-dbo {:birth_date "1997-01-01"})))
                      (into (repeatedly 5 #(factory/patient-dbo {:birth_date "1980-01-01"})))
                      (into (repeatedly 5 #(factory/patient-dbo {:birth_date "1930-01-01"}))))
          update-call-args (atom nil)]
      (with-redefs [repo/list-patients (constantly dataset)
                    repo/update-aggregation-data! (fn [_ & args]
                                                    (reset! update-call-args (vec args)))]
        (aggregator/aggregate-patient-age-groups! nil)
        (is (= "patient-age-group" (get @update-call-args 0)))
        (is (= [{:label "Children", :value 5}
                {:label "Adults", :value 5}
                {:label "Older Adults", :value 5}]
               (get @update-call-args 1)))))))

(deftest aggregate-patient-language-groups-test
  (let [dataset (-> []
                    (into (repeatedly 5 #(factory/patient-dbo {:language "en-IN"})))
                    (into (repeatedly 5 #(factory/patient-dbo {:language "en-GB"})))
                    (into (repeatedly 5 #(factory/patient-dbo {:language "en-US"}))))
        update-call-args (atom nil)]
    (with-redefs [repo/list-patients (constantly dataset)
                  repo/update-aggregation-data! (fn [_ & args]
                                                  (reset! update-call-args (vec args)))]
      (aggregator/aggregate-patient-language-groups! nil)
      (is (= "patient-language" (get @update-call-args 0)))
      (is (= [{:label "en-IN", :value 5}
              {:label "en-GB", :value 5}
              {:label "en-US", :value 5}]
             (get @update-call-args 1))))))

(deftest aggregate-patient-marital-status-groups-test
  (let [dataset (-> []
                    (into (repeatedly 5 #(factory/patient-dbo {:marital_status "S"})))
                    (into (repeatedly 5 #(factory/patient-dbo {:marital_status "M"})))
                    (into (repeatedly 5 #(factory/patient-dbo {:marital_status "D"}))))
        update-call-args (atom nil)]
    (with-redefs [repo/list-patients (constantly dataset)
                  repo/update-aggregation-data! (fn [_ & args]
                                                  (reset! update-call-args (vec args)))]
      (aggregator/aggregate-patient-marital-status-groups! nil)
      (is (= "patient-marital-status" (get @update-call-args 0)))
      (is (= [{:label "Single" :value 5}
              {:label "Married" :value 5}
              {:label "Divorced" :value 5}]
             (get @update-call-args 1))))))
