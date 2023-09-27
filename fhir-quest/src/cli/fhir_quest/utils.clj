(ns fhir-quest.utils
  (:require [java-time.api :as jt])
  (:import java.time.temporal.ChronoUnit))

(defn classify [groups default-group value]
  (reduce (fn [assigned [group [start end]]]
            (if (and (<= start value) (<= value end))
              group
              assigned))
          default-group
          groups))

(defn classifier [groups default-group]
  (fn [v] (classify groups default-group v)))

(defn count-by [grouping-fn coll]
  (reduce (fn [counts item]
            (let [group (grouping-fn item)]
              (assoc counts group (inc (get counts group 0)))))
          {}
          coll))

(defn months-since [date]
  (-> date
      (jt/local-date)
      (#(.between ChronoUnit/MONTHS % (jt/local-date)))))

(defn average
  ([quantities] (average quantities + /))
  ([quantities sum-fn div-fn]
   (if (empty? quantities)
     0
     (div-fn (reduce sum-fn (first quantities) (rest quantities))
             (count quantities)))))
