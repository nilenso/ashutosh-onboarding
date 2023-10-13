(ns clinic.fhir.utils)

(defn find-code
  "Returns the value of the `code` attribute corresponding to the given `system`
   in a FHIR Codeable Concept."
  [system codeable-concept]
  (some->> codeable-concept
           (:coding)
           (filter #(= system (get % :system)))
           (first)
           (:code)))

(defn find-value
  "Returns the value corresponding to the given `system` in a collection of
   `elements`."
  [system elements]
  (some->> elements
           (filter #(= system (% :system)))
           (first)
           (:value)))
