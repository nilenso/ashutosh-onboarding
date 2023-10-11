(ns clinic.components
  (:require [reagent.core :as r]))

(defn page []
  (let [this (r/current-component)]
    [:main {:class ["flex" "flex-col gap-12 md:gap-16"
                    "w-full max-w-4xl"
                    "mx-auto p-8 md:p-12"]}
     [:header
      [:h1 {:class "text-3xl md:text-4xl"} "Acme Orthopedic Clinic"]]
     (into [:section (r/props this)]
           (r/children this))]))

(defn heading-2 [text]
  [:h2 {:class ["text-xl" "md:text-2xl"]} text])
