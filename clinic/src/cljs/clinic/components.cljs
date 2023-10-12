(ns clinic.components
  (:require [reagent.core :as r]))

(defn heading-1 [text]
  [:h1 {:class "text-3xl md:text-4xl"} text])

(defn heading-2 [text]
  [:h2 {:class ["text-xl" "md:text-2xl"]} text])

(defn page []
  (let [this (r/current-component)
        props (r/props this)
        logout-enabled (props :logout-enabled)
        logout-handler (props :on-logout-click #())]
    (into [:main {:class ["flex" "flex-col gap-12 md:gap-16"
                          "w-full max-w-4xl"
                          "mx-auto p-8 md:p-12"]}
           [:header {:class ["flex" "flex-row" "gap-12"]}
            [heading-1 "Acme Orthopedic Clinic"]
            (when logout-enabled
              [:<>
               [:div {:class "flex-grow"}]
               [:button {:on-click logout-handler
                         :class ["bg-transparent" "hover:bg-blue-500" "text-blue-700"
                                 "hover:text-white" "px-4" "py-2"
                                 "border" "border-blue-500"
                                 "hover:border-transparent" "rounded-full"]}
                "Logout"]])]]
          (r/children this))))
