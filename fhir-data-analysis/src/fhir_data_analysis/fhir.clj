(ns fhir-data-analysis.fhir
  (:require [cheshire.core :as json]
            [java-time.api :as jt])
  (:import [java.time Duration]))

(defn read-bundle-entries
  "JSON-parses a FHIR Bundle entity from the given reader and returns a (lazy)
   collection of its entries."
  [json-reader]
  (->> json-reader
       (#(json/parse-stream % keyword))
       (#(get % :entry []))
       (map #(get % :resource))))

(defn filter-entries
  "Filters the given `entries` by the value of their `resourceType` attribute."
  [resourceType entries]
  (filter #(= resourceType (get % :resourceType)) entries))

(defn- period->duration [period]
  (Duration/between
   (jt/offset-date-time (get period :start))
   (jt/offset-date-time (get period :end))))

(defn- average
  ([quantities] (average quantities + /))
  ([quantities sum-fn div-fn]
   (if (empty? quantities)
     0
     (div-fn (reduce sum-fn (first quantities) (rest quantities))
             (count quantities)))))

(defn- average-duration [durations]
  (average durations #(.plus %1 %2) #(.dividedBy %1 %2)))

(defn- duration->string [d]
  (let [seconds (.toSeconds d)]
    (str (quot seconds 3600) "h "
         (quot (mod seconds 3600) 60) "m "
         (mod seconds 60) "s")))

(defn encounter-duration-avg [entries]
  (->> entries
       (filter-entries "Encounter")
       (map #(period->duration (get % :period)))
       (average-duration)
       (duration->string)))

(defn encounter-duration-avg-by-subject [entries]
  (->> entries
       (filter-entries "Encounter")
       (map #(do {:subject-ref (get-in % [:subject :reference])
                  :duration (period->duration (get % :period))}))
       (group-by #(get % :subject-ref))
       (reduce-kv #(assoc %1 %2 (->> %3
                                     (map :duration)
                                     (average-duration)
                                     (duration->string)))
                  {})))

(defn- encounter-durations-by-location [entries]
  (->> entries
       (filter-entries "Encounter")
       (map #(do {:locations (vec (map (fn [l] (get-in l [:location :reference]))
                                       (get % :location)))
                  :duration (period->duration (get % :period))}))
       (reduce
        (fn [location-durations encounter]
          (loop [lds location-durations
                 [l & ls] (get encounter :locations)
                 d (get encounter :duration)]
            (cond
              (nil? l) lds
              (contains? lds l) (recur (update lds l conj d) ls d)
              :else (recur (assoc lds l [d]) ls d))))
        {})))

(defn encounter-duration-avg-by-location [entries]
  (->> entries
       (encounter-durations-by-location)
       (reduce-kv #(assoc %1 %2 (->> %3
                                     (average-duration)
                                     (duration->string)))
                  {})))

(defn- synthea-location-ref [identifiers]
  (->> identifiers
       (filter #(= (get % :system) "https://github.com/synthetichealth/synthea"))
       (first)
       (#(str "Location?identifier=" (get % :system) "|" (get % :value)))))

(defn- location-city-mapping [locations]
  (->> locations
       (map #(do {:ref (synthea-location-ref (get % :identifier))
                  :city (get-in % [:address :city])}))
       (reduce #(assoc %1 (get %2 :ref) (get %2 :city)) {})))

(defn encounter-duration-avg-by-city [locations entries]
  (let [location-cities (location-city-mapping locations)]
    (->> entries
         (encounter-durations-by-location)
         (reduce-kv (fn [city-durations location durations]
                      (let [city (get location-cities location :unkown-city)]
                        (if (contains? city-durations city)
                          (update city-durations city into durations)
                          (assoc city-durations city durations))))
                    {})
         (reduce-kv #(assoc %1 %2 (->> %3
                                       (average-duration)
                                       (duration->string)))
                    {}))))

(defn subject-encounter-duration-avg-by-organization [entries]
  (->> entries
       (filter-entries "Encounter")
       (map #(do {:subject-ref (get-in % [:subject :reference])
                  :organization-ref (get-in % [:serviceProvider :reference])
                  :duration (period->duration (get % :period))}))
       (reduce
        (fn [grouped encounter]
          (let [sref (get encounter :subject-ref)
                oref (get encounter :organization-ref)
                dur (get encounter :duration)
                omap (get grouped sref {})]
            (if (not (contains? grouped sref))
              (assoc grouped sref {oref [dur]})
              (assoc grouped sref (if (contains? omap oref)
                                    (update omap oref conj dur)
                                    (assoc omap oref [dur]))))))
        {})
       (reduce-kv (fn [averages sref org-encounters]
                    (assoc averages
                           sref
                           (reduce-kv #(->> %3
                                            (average-duration)
                                            (duration->string)
                                            (assoc %1 %2))
                                      {}
                                      org-encounters)))
                  {})))

(defn insurance-claimed-sum-by-subject [entries]
  (->> entries
       (filter-entries "Claim")
       (map #(do {:subject-ref (get-in % [:patient :reference])
                  :amount (get-in % [:total :value])
                  :currency (get-in % [:total :currency])}))
       (group-by #(get % :subject-ref))
       (reduce-kv #(assoc %1 %2 (group-by :currency %3)) {})
       (reduce-kv (fn [averages sref currency-amounts]
                    (assoc averages
                           sref
                           (reduce-kv #(->> %3
                                            (map :amount)
                                            (average)
                                            (assoc %1 %2))
                                      {}
                                      currency-amounts)))
                  {})))

(defn count-patients [month entries]
  (->> entries
       (filter-entries "Encounter")
       (map #(do {:subject-ref (get-in % [:subject :reference])
                  :month (.getMonth
                          (jt/offset-date-time (get-in % [:period :start])))}))
       (filter #(= (get % :month) (jt/month month)))
       (reduce #(conj %1 (get %2 :subject-ref)) #{})
       (count)))
