(ns clinic.factory
  (:require [clinic.specs.patient :as specs]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn- generate-date []
  (String/format "%04d-%02d-%02d"
                 (into-array [(+ 1970 (rand-int 52))
                              (inc (rand-int 12))
                              (inc (rand-int 28))])))

(defn- with-generator-fn [gen-fn]
  (-> (fn [& _] (gen-fn))
      (gen/fmap (gen/return nil))
      (constantly)))

(defn create-params []
  (->> {::specs/birth-date (with-generator-fn generate-date)}
       (s/gen ::specs/create-params)
       (gen/generate)))
