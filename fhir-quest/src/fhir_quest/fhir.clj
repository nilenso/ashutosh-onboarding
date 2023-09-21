(ns fhir-quest.fhir
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [java-time.api :as jt]))

(defn read-bundle
  "JSON-parses a FHIR Bundle from file `f` and returns a lazy collection of its
   entries."
  [f]
  (with-open [r (io/reader f)]
    (->> r
         (#(json/parse-stream % keyword))
         (#(get % :entry []))
         (map #(get % :resource)))))

(defn read-bundles
  "JSON-parses FHIR Bundles using JSON files in directory `die` and returns a
   lazy collection of their entries."
  [dir]
  (->> dir
       (io/file)
       (file-seq)
       (filter #(.endsWith (.getName ^java.io.File %) ".json"))
       (pmap read-bundle)
       (apply concat)))

(defn resource-type
  "Returns the `resourceType` of a FHIR resource."
  [e]
  (get e :resourceType))

(defn id
  "Returns the `id` of a FHIR resource."
  [e]
  (get e :id))

(defn patient-by-age-classifier
  "Returns a closure on the given `age-groups` that accepts a db spec and an
   entry and returns a corresponding age-group. Both bounds of the age range in
   `age-groups` are inclusive.

       ((age-classifier {:first [0 10]
                         :second [11 20]}) db-spec fhir-entry)
       ;=> :first
   "
  [age-groups]
  (let [cy (.getValue (jt/year))]
    (fn [p]
      (let [age (-> p
                    (get :birthDate)
                    (jt/local-date)
                    (.getYear)
                    (#(- cy %)))]
        (reduce-kv (fn [assigned group [start end]]
                     (if (and (<= start age) (<= age end))
                       group
                       assigned))
                   :unknown
                   age-groups)))))
