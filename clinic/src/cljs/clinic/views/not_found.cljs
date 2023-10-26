(ns clinic.views.not-found
  (:require [clinic.components :as components]
            [reagent.core :as r]))

(defn root []
  (let [{:keys [title message]
         :or {title "Page Not Found!"
              message "Not sure what you're looking for, but it isn't here."}}
        (r/props (r/current-component))]
    [:section {:class ["flex" "flex-col" "gap-4" "items-center"]}
     [components/heading-1 "( ͡° ͜ʖ ͡°)_/¯"]
     [components/heading-2 title]
     [:p message]]))
