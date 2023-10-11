(ns clinic.components
  (:require [clinic.user-role.core :as user-role]
            [reagent.core :as r]))

(defn page []
  (let [this (r/current-component)
        props (dissoc (r/props this) :logout-enabled)
        logout-enabled (get (r/props this) :logout-enabled)]
    [:main {:class ["flex" "flex-col gap-12 md:gap-16"
                    "w-full max-w-4xl"
                    "mx-auto p-8 md:p-12"]}
     [:header {:class ["flex" "flex-row" "gap-12"]}
      [:h1 {:class "text-3xl md:text-4xl"} "Acme Orthopedic Clinic"]
      (when logout-enabled
        [:<>
         [:div {:class "flex-grow"}]
         [:button {:on-click user-role/clear
                   :class ["bg-transparent" "hover:bg-blue-500" "text-blue-700"
                           "hover:text-white" "px-4" "py-2"
                           "border" "border-blue-500"
                           "hover:border-transparent" "rounded-full"]}
          "Logout"]])]
     (into [:section (dissoc props)]
           (r/children this))]))

(defn heading-2 [text]
  [:h2 {:class ["text-xl" "md:text-2xl"]} text])
