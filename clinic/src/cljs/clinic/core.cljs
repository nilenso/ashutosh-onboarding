(ns clinic.core
  (:require [clinic.router :as router]
            [clinic.views.core :as views]
            [reagent.dom :as dom]))

(defn mount-root []
  (dom/render [views/root]
              (.getElementById js/document "app")))

(defn ^:export init! []
  (router/start!)
  (mount-root))
