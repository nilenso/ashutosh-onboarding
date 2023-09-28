(ns fhir-quest.factory)

(defn- deep-merge [& maps]
  (reduce
   (fn [result current]
     (merge-with
      (fn [x y]
        (cond (map? y) (deep-merge x y)
              :else y))
      result
      current))
   (first maps)
   (rest maps)))

(defn encounter [values]
  (deep-merge {:resourceType "Encounter"
               :id (random-uuid)
               :subject {:reference (str "urn:uuid:" (random-uuid))}
               :period {:start "2023-09-28T00:00:00+05:30"
                        :end "2023-09-28T00:05:00+05:30"}}
              values))

(defn patient [values]
  (deep-merge {:resourceType "Patient"
               :id (random-uuid)
               :birthDate "2000-01-01"
               :communication [{:language {:coding [{:system "test-system"
                                                     :code "unknown"}]}}]
               :maritalStatus {:coding [{:system "test-system"
                                         :code "unknown"}]}}
              values))
