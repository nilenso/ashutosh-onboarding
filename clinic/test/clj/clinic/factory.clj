(ns clinic.factory
  (:require [clinic.specs.patient :as specs]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import java.time.LocalDate))

(defn- rand-date []
  (->> 30000 ; ~ 82 years
       (rand-int)
       (.minusDays (LocalDate/now))
       (.toString)))

(defn- rand-phone []
  ;; the goal here is NOT to generate a phone number that conforms to a
  ;; national/international formatting standard. We just need seemingly valid
  ;; phone numbers for all intents and purposes.
  (String/format (rand-nth ["(%03d) %03d-%04d"
                            "%03d-%03d-%04d"
                            "+01 %03d %03d %04d"
                            "%03d%03d%04d"])
                 (into-array [(rand-int 999)
                              (rand-int 999)
                              (rand-int 9999)])))

(defn- with-generator-fn [gen-fn]
  (-> (fn [& _] (gen-fn))
      (gen/fmap (gen/return nil))
      (constantly)))

(defn create-params []
  (->> {::specs/birth-date (with-generator-fn rand-date)
        ::specs/phone (with-generator-fn rand-phone)}
       (s/gen ::specs/create-params)
       (gen/generate)))
