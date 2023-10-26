(ns clinic.utils
  (:require [clojure.spec.alpha :as s]))

(defn form-data->map
  "Converts DOM FormData to a Clojure map. Also keywordizes keys in the
   resulting map and then removes `optional-keyset` from it if the corresponding
   value is empty."
  [form-data optional-keyset]
  (->> form-data
       (.entries)
       (map (fn [[k v]] [(keyword k) v]))
       (remove #(and (contains? optional-keyset (first %))
                     (empty? (second %)))) ; remove empty fields that are optional
       (into {})))

(defn invalid-keys
  "Returns a set of keys whose values don't conform to the given `spec` for the
   given `data`."
  [spec data]
  (->> data
       (s/explain-data spec)
       (::s/problems)
       (map :in)
       (flatten)
       (set)))

(defn query-params
  "Returns a keywordized map of query parameters in the given `url`."
  [url]
  (->> (js/URL. url "http://dummy")
       (.-searchParams)
       (map (fn [[k v]] [(keyword k) v]))
       (into {})))
