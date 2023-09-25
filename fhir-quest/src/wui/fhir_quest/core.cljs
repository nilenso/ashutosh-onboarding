(ns fhir-quest.core
  (:require [reagent.dom :as dom]))

(defn mount-root []
  (dom/render [:p "Hello, world!"] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
