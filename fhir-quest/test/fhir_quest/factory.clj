(ns fhir-quest.factory
  (:require [cheshire.core :as json]))

(defn- deep-merge [& maps]
  (reduce
   (fn [result current]
     (merge-with
      (fn [x y]
        (cond (map? y) (deep-merge x y)
              :else y))
      result
      current))
   (first maps)
   (rest maps)))

(defn- rand-date []
  (String/format "%04d-%02d-%02d"
                 (into-array [(+ 1950 (rand-int 75))
                              (+ 1 (rand-int 12))
                              (+ 1 (rand-int 28))])))

(defn- rand-language []
  (rand-nth ["bn-IN"
             "en-GB"
             "en-IN"
             "en-US"
             "fr-FR"
             "hi-IN"
             "ta-IN"]))

(defn- rand-marital-status []
  (rand-nth ["D" "M" "S" "W"]))

(defn encounter
  ([] (encounter {}))
  ([values]
   (deep-merge {:resourceType "Encounter"
                :id (str (random-uuid))
                :subject {:reference (str "urn:uuid:" (random-uuid))}
                :period {:start "2023-09-28T00:00:00+05:30"
                         :end "2023-09-28T00:05:00+05:30"}}
               values)))

(defn encounter-dbo []
  {:id (str (random-uuid))
   :subject_id (str (random-uuid))
   :duration_ms (rand-int 999999999)})

(defn patient
  ([] (patient {}))
  ([values]
   (deep-merge {:resourceType "Patient"
                :id (str (random-uuid))
                :birthDate (rand-date)
                :communication [{:language {:coding [{:system "urn:ietf:bcp:47"
                                                      :code (rand-language)}]}}]
                :maritalStatus {:coding [{:system "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus"
                                          :code (rand-marital-status)}]}}
               values)))

(defn patient-dbo []
  {:id (str (random-uuid))
   :birth_date (rand-date)
   :language (rand-language)
   :marital_status (rand-marital-status)})

(defn aggregation-dbo
  ([] (aggregation-dbo {}))
  ([values]
   (deep-merge {:id (str (random-uuid))
                :description "test-description"
                :chart_type "test-chart-type"
                :data_json (json/generate-smile [{:label "test-label"
                                                  :value (rand-int 9999999)}])}
               values)))
