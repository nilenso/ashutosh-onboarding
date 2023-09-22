(ns fhir-quest.utils
  (:require [java-time.api :as jt])
  (:import java.time.temporal.ChronoUnit))

(defn classify [groups default-group value]
  (reduce-kv (fn [assigned group [start end]]
               (if (and (<= start value) (<= value end))
                 group
                 assigned))
             default-group
             groups))

(defn classifier [groups default-group]
  (fn [v] (classify groups default-group v)))

(defn months-since [date]
  (-> date
      (jt/local-date)
      (#(.between ChronoUnit/MONTHS % (jt/local-date)))))

(defn count-unique [items]
  (reduce (fn [counts item]
            (if (contains? counts item)
              (update counts item inc)
              (assoc counts item 1)))
          {}
          items))
