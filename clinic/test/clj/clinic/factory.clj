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

(defn- with-generator-fn [gen-fn]
  (-> (fn [& _] (gen-fn))
      (gen/fmap (gen/return nil))
      (constantly)))

(defn create-params []
  (->> {::specs/birth-date (with-generator-fn rand-date)}
       (s/gen ::specs/create-params)
       (gen/generate)))
