(ns clinic.specs.patient
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def ^:private not-blank? (complement string/blank?))

(defn phone-number? [v]
  ;; Not strictly checking the input sequence for digits and allowing room for
  ;; phone number formatting characters. Taking the number of digits in a phone
  ;; number from the E.164 standard. https://en.wikipedia.org/wiki/E.164
  (and (re-matches #"\+?[\d-()x\[\]\. ]+" v)
       (<= 8 (count (re-seq #"\d" v)) 15)))

(defn- date? [v]
  #?(:clj (try (java.time.LocalDate/parse v)
               true
               (catch Exception _ false))
     :cljs ((complement NaN?) (js/Date.parse v))))

(s/def ::id (s/and string? not-blank?))
(s/def ::first-name (s/and string? not-blank?))
(s/def ::last-name (s/and string? not-blank?))
(s/def ::birth-date (s/and string?
                           (partial re-matches #"\d{4}-\d{2}-\d{2}") ; keep regex becaue js will parse partial dates.
                           date?))
(s/def ::gender #{"male" "female" "other" "unknown"})
(s/def ::marital-status (s/nilable #{"A" "D" "I" "L" "M" "P" "S" "T" "U" "W" "UNK"}))
(s/def ::email (s/nilable (s/and string? not-blank?)))
(s/def ::phone (s/nilable (s/and string? phone-number?)))

(s/def ::create-params
  (s/keys :req-un [::first-name ::last-name ::birth-date ::gender ::phone]
          :opt-un [::marital-status ::email]))

(s/def ::patient
  (s/keys :req-un [::id ::first-name ::last-name ::birth-date ::gender ::phone]
          :opt-un [::marital-status ::email]))
