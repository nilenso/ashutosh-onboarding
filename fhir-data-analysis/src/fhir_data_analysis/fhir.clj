(ns fhir-data-analysis.fhir
  (:require [cheshire.core :as json]))

(defn read-bundle-entries
  "JSON-parses a FHIR Bundle entity from the given reader and returns a (lazy)
   collection of its entries."
  [json-reader]
  (->> json-reader
       (#(json/parse-stream % keyword))
       (#(get % :entry []))
       (map #(get % :resource))))

(defn- filter-entries [resourceType entries]
  (filter #(= resourceType (get % :resourceType)) entries))

(defn- extension-value [e]
  (let [url (get e :url)]
    (case url
      "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race"
      {:race (get-in e [:extension 0 :valueCoding :code])}
      "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity"
      {:ethinicity (get-in e [:extension 0 :valueCoding :code])}
      "http://synthetichealth.github.io/synthea/disability-adjusted-life-years"
      {:disability-adjusted-life-years (get e :valueDecimal)}
      "http://synthetichealth.github.io/synthea/quality-adjusted-life-years"
      {:quality-adjusted-life-years (get e :valueDecimal)}
      nil)))

(defn- extension-data [p]
  (reduce #(merge %1 (extension-value %2)) {} (get p :extension)))

(defn patients
  "Finds Patient entities in the entries, extracts relevant data from these and
   returns a (lazy) collection of extracted Patient entries."
  [entries]
  (->> entries
       (filter-entries "Patient")
       (map #(into {:id (get % :id)
                    :gender (get % :gender)
                    :marital-status (get-in % [:maritalStatus :coding 0 :code])
                    :birth-date (get % :birthDate)
                    :deceased (get % :deceasedDateTime)
                    :address {:city (get-in % [:address 0 :city])
                              :state (get-in % [:address 0 :state])
                              :country (get-in % [:address 0 :country])}
                    :language (get-in % [:communication 0 :language :coding 0 :code])}
                   (extension-data %)))))

(defn encounters
  "Finds Encounter entities in the entries, extracts relevant data from these
   and returns a (lazy) collection of extracted Encounter entries."
  [entries]
  (->> entries
       (filter-entries "Encounter")
       (map
        (fn [e]
          {:id (get e :id)
           :organization-ref (get-in e [:serviceProvider :reference])
           :patient-ref (get-in e [:subject :reference])
           :status (get e :status)
           :class (get-in e [:class :code])
           :type (get-in e [:type 0 :coding 0 :code])
           :period-start (get-in e [:period :start])
           :period-end (get-in e [:period :end])
           :participants (vec
                          (map
                           (fn [p]
                             {:type (get-in p [:type 0 :coding 0 :code])
                              :individual-ref (get-in p [:individual :reference])
                              :period-start (get-in p [:period :start])
                              :period-end (get-in p [:period :end])})
                           (get e :participant)))}))))
