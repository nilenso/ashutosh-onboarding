(ns clinic.specs.patient
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def ^:private not-blank? (complement string/blank?))

(s/def ::id (s/and string? not-blank?))
(s/def ::mrn (s/and string? (partial re-matches #"\d{3}-\d{3}-\d{3}")))
(s/def ::first-name (s/and string? not-blank?))
(s/def ::last-name (s/and string? not-blank?))
(s/def ::birth-date (s/and string? (partial re-matches #"\d{4}-\d{2}-\d{2}")))
(s/def ::gender #{"male" "female" "other" "unknown"})
(s/def ::marital-status (s/nilable #{"A" "D" "I" "L" "M" "P" "S" "T" "U" "W" "UNK"}))
(s/def ::email (s/nilable (s/and string? not-blank?)))
(s/def ::phone (s/nilable (s/and string? not-blank?)))

(s/def ::create-params
  (s/keys :req-un [::first-name ::last-name ::birth-date ::gender]
          :opt-un [::marital-status ::email ::phone]))

(s/def ::patient
  (s/keys :req-un [::id ::mrn ::first-name ::last-name ::birth-date ::gender]
          :opt-un [::marital-status ::email ::phone]))
