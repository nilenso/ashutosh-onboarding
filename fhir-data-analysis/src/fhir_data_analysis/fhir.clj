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

(defn- map-transformer
  [transformations]
  (fn transform [entry]
    (reduce
     (fn reducer [transformed [k [entry-ks tfn :as v]]]
       (cond
         (map? v) (assoc transformed k (reduce reducer (get transformed k) v))
         (vector? entry-ks) (assoc transformed k (tfn (get-in entry entry-ks)))
         :else (assoc transformed k (tfn (get entry entry-ks)))))
     {}
     transformations)))

(defn- period->duration [period]
  (Duration/between
   (jt/offset-date-time (get period :start))
   (jt/offset-date-time (get period :end))))

(defn- average-duration [durations]
  (.dividedBy
   (reduce #(.plus %1 %2) (first durations) (rest durations))
   (long (count durations))))

(defn- duration->string [d]
  (let [seconds (.toSeconds d)]
    (str
     (quot seconds 3600) "h "
     (quot (mod seconds 3600) 60) "m "
     (mod seconds 60) "s")))

(defn encounter-duration-avg [entries]
  (->> entries
       (filter-entries "Encounter")
       (map (map-transformer {:duration [:period period->duration]}))
       (map #(get % :duration))
       (average-duration)
       (duration->string)))

(defn subject-encounter-duration-avg [entries]
  (->> entries
       (filter-entries "Encounter")
       (map (map-transformer {:subject-ref [[:subject :reference] identity]
                              :duration [:period period->duration]}))
       (group-by #(get % :subject-ref))
       (reduce-kv #(assoc %1 %2 (->> %3
                                     (map :duration)
                                     (average-duration)
                                     (duration->string)))
                  {})))

(defn- encounter-durations-by-location [entries]
  (->> entries
       (filter-entries "Encounter")
       (map
        (map-transformer
         {:locations [:location
                         ;; transformation fn to inline location references
                      (fn [locations]
                        (map #(get-in % [:location :reference]) locations))]
          :duration [:period period->duration]}))
       (reduce
        (fn [location-durations encounter]
          (loop [lds location-durations
                 [l & ls] (get encounter :locations)
                 d (get encounter :duration)]
            (cond
              (nil? l) lds
              (contains? lds l)
              (recur (update lds l conj d) ls d)
              :else
              (recur (assoc lds l [d]) ls d))))
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
       (map (map-transformer {:ref [:identifier synthea-location-ref]
                              :city [[:address :city] identity]}))
       (reduce #(assoc %1 (get %2 :ref) (get %2 :city)) {})))

(defn encounter-duration-avg-by-city [locations entries]
  (let [location-cities (location-city-mapping locations)]
    (->> entries
         (encounter-durations-by-location)
         (reduce-kv
          (fn [city-durations location durations]
            (let [city (get location-cities location :unkown-city)]
              (if (contains? city-durations city)
                (update city-durations city into durations)
                (assoc city-durations city durations))))
          {})
         (reduce-kv #(assoc %1 %2 (->> %3
                                       (average-duration)
                                       (duration->string)))
                    {}))))
