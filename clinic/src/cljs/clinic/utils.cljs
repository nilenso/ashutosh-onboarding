(ns clinic.utils)

(defn form-data->map
  "Converts DOM FormData to a Clojure map. Also keywordizes keys and filters
   out empty values from the resulting map."
  [form-data]
  (->> form-data
       (.entries)
       (map vec)
       (remove #(empty? (second %))) ; remove empty fields
       (reduce #(assoc %1 (keyword (first %2)) (second %2)) {})))
