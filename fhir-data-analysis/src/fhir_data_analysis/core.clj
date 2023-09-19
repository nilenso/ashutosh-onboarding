(ns fhir-data-analysis.core
  (:require [clojure.java.io :as io]
            [fhir-data-analysis.fhir :as fhir]))

(defn- fhir-bundle-entries
  "Returns a lazy sequence of bundle entries for FHIR JSON files in the given
   directory."
  [dir]
  (->> dir
       (io/file)
       (file-seq)
       (filter #(.endsWith (.getName ^java.io.File %) ".json"))
       (pmap
        (fn [f]
          (with-open [r (io/reader f)]
            (fhir/read-bundle-entries r))))
       (apply concat)))

(defn -main [& _]
  (let [locations (->> "output/fhir"
                       (fhir-bundle-entries)
                       (fhir/filter-entries "Location")
                       (vec))]
    (->> "output/fhir"
         (fhir-bundle-entries)
         (fhir/patient-distribution (fhir/patient-age-classifier {:0-25 [0 25]
                                                                  :26-50 [26 50]
                                                                  :51-75 [51 75]
                                                                  :76-100 [76 100]})))))
