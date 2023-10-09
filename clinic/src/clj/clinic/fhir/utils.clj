(ns clinic.fhir.utils)

(defn find-code
  "Returns the value of the `code` attribute corresponding to the given `system`
   in a FHIR Codeable Concept."
  [system codeable-concept]
  (some-> codeable-concept
          (get :coding)
          ((partial filter #(= system (get % :system))))
          (first)
          (get :code)))

(defn find-value
  "Returns the value corresponding to the given `system` in a collection of
   `elements`."
  [system elements]
  (some-> elements
          ((partial filter #(= system (% :system))))
          (first)
          (get :value)))
