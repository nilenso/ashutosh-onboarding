(ns fhir-quest.fhir
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [java-time.api :as jt])
  (:import java.time.Duration)
  (:import java.time.temporal.ChronoUnit))

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

(defn- find-code [system codeable-concept]
  (->> (get codeable-concept :coding)
       (filter #(= system (get % :system)))
       (first)
       (#(get % :code))))

(defn- period->duration [period]
  (Duration/between
   (jt/offset-date-time (get period :start))
   (jt/offset-date-time (get period :end))))

(defn resource-type
  "Returns the `resourceType` of a FHIR resource."
  [e]
  (get e :resourceType))

(defn id
  "Returns the `id` of a FHIR resource."
  [e]
  (get e :id))

(defn encounter-subject-id
  "Returns the id of the subject for the given encounter resource."
  [e]
  (-> e
      (get-in [:subject :reference])
      (.replace "urn:uuid:" "")))

(defn encounter-duration-ms
  "Returns the duration in milliseconds in the encounter resource."
  [e]
  (-> e
      (get :period)
      (period->duration)
      (.toMillis)))

(defn patient-age
  "Returns the age of the given patient resource based on their `birthYear` and
   current date."
  [p]
  (->> (get p :birthDate)
       (jt/local-date)
       (#(.between ChronoUnit/YEARS % (jt/local-date)))))

(defn patient-language
  "Returns the communication language code corresponding to the given coding
   system for the given patient resource."
  [p system]
  (->> (get p :communication)
       (filter #(contains? % :language))
       (first)
       (#(get % :language))
       (find-code system)))

(defn patient-marital-status
  "Returns the marital status code corresponding to the given coding system
   `system` for the given patient resource `p`."
  [p system]
  (->> (get p :maritalStatus)
       (find-code system)))
