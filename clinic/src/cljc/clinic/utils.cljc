(ns clinic.utils)

(defn extract-digits [s]
  (apply str (re-seq #"\d" s)))
