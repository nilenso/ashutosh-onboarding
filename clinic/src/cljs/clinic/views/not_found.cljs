(ns clinic.views.not-found
  (:require [clinic.components :as components]))

(defn root []
  [:section {:class ["flex" "flex-col" "gap-4" "items-center"]}
   [components/heading-1 "( ͡° ͜ʖ ͡°)_/¯"]
   [components/heading-2 "Page Not Found!"]
   [:p "Not sure what you're looking for, but it isn't here."]])
