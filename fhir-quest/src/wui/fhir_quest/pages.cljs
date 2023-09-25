(ns fhir-quest.pages
  (:require [ajax.core :refer [GET]]
            [fhir-quest.components :as components]
            [react :as react]
            [reagent.core :as r]))

(defn query-selector [selection-handler]
  (let [loading (r/atom true)
        queries (r/atom nil)]
    (GET "/api/v1/query" {:format "json"
                          :handler #(reset! queries %1)
                          :finally #(reset! loading false)})
    (fn [selection-handler]
      [:div {:class ["self-center"
                     "flex flex-col md:flex-row items-center gap-4"]}
       (if @loading
         [components/spinner]
         [:<>
          [:label {:for "queries" :class "text-lg"} "Aggregation:"]
          (into [:select {:name "queries"
                          :class ["py-2 px-4 pr-9 w-full"
                                  "border rounded-full border-gray-300 focus:border-blue-500"
                                  "text-sm"]
                          :on-change (fn [e]
                                       (let [v (-> e .-target .-value)]
                                         (selection-handler (if (empty? v) nil v))))}
                 [:option {:defaultValue true
                           :value ""}
                  "Select a query..."]]

                (for [{id "id" desc "description"} @queries]
                  [:option {:value id} desc]))])])))

(def chart-components {"scalar" components/scalar-chart
                       "bar" components/bar-chart
                       "pie" components/pie-chart})

(defn query-chart [query-id]
  (let [[loading set-loading] (react/useState true)
        [chart set-chart] (react/useState nil)]
    (react/useEffect (fn []
                       (GET (str "/api/v1/query/" query-id "/chart")
                         {:format "json"
                          :handler #(set-chart %1)
                          :finally #(set-loading false)}))
                     (array query-id))
    [:div {:class ["self-center"]}
     (if loading
       [components/spinner]
       [(get chart-components (get chart "type")) (get chart "data")])]))

(defn home []
  (let [selected-query-id (r/atom nil)]
    (fn []
      [:div {:class ["flex" "flex-col gap-8 md:gap-12"
                     "w-full max-w-4xl"
                     "mx-auto"
                     "p-8 md:p-12"]}
       [:h1 {:class ["text-3xl md:text-4xl"]} "FHIR Quest"]
       [query-selector #(reset! selected-query-id %)]
       (when @selected-query-id
         [:f> query-chart @selected-query-id])])))
