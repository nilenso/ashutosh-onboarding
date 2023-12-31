(ns fhir-quest.core
  (:require [fhir-quest.pages :as pages]
            [reagent.dom :as dom]))

(defn mount-root []
  (dom/render [pages/home] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
