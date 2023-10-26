(ns clinic.views.not-found
  (:require [clinic.components :as components]))

(defn root [title message]
  [:section {:class ["flex" "flex-col" "gap-4" "items-center"]}
   [components/heading-1 "( ͡° ͜ʖ ͡°)_/¯"]
   [components/heading-2 (or title "Page Not Found!")]
   [:p (or message "Not sure what you're looking for, but it isn't here.")]])
