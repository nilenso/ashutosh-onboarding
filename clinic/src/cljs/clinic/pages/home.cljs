(ns clinic.pages.home
  (:require [clinic.components :as components]
            [clinic.user-role.core :as user-role]))

(defn- role-selector []
  (let [current-role (user-role/get)
        !select (atom nil)]
    (fn []
      [:div {:class ["flex" "flex-col" "gap-8" "items-center"]}
       [components/heading-2 "Login As"]
       [:div {:class ["flex" "flex-row" "gap-6"]}
        [:select {:defaultValue (or @current-role "doctor")
                  :class ["py-2 px-4 pr-9 w-32"
                          "border rounded-full border-gray-300 focus:border-blue-500"
                          "text-sm"]
                  :ref (partial reset! !select)}
         [:option {:value "doctor"} "Doctor"]
         [:option {:value "nurse"} "Nurse"]
         [:option {:value "patient"} "Patient"]]
        [:button {:class ["bg-blue-500" "hover:bg-blue-700" "text-white"
                          "font-bold" "py-2" "px-4" "rounded-full"]
                  :on-click #(-> @!select
                                 (.-value)
                                 (user-role/set))}
         "Go"]]])))

(defn- nurse-fn-list []
  (let [list-item #(vector :li
                           [:a {:href %2
                                :class ["text-blue-600" "hover:underline"]}
                            %1])]

    [:div {:class ["flex" "flex-col" "gap-8"]}
     [components/heading-2 "Operations"]
     [:ol {:class ["list-decimal" "list-inside"]}
      [list-item "Add patient" "/patients/new"]]]))

(defn root []
  (let [current-role (user-role/get)]
    (fn []
      [components/page {:logout-enabled @current-role}
       (case @current-role
         "nurse" [nurse-fn-list]
         [role-selector])])))
