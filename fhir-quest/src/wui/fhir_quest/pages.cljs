(ns fhir-quest.pages
  (:require [fhir-quest.components :as components]
            [fhir-quest.events]
            [fhir-quest.subs]
            [re-frame.core :as rf]))

(defn- aggregation-selector [_]
  (let [queries (rf/subscribe [:fhir-quest.subs/agg-selector-items])
        error (rf/subscribe [:fhir-quest.subs/agg-selector-error])
        loading (rf/subscribe [:fhir-quest.subs/agg-selector-loading])]
    (fn [selection-handler-fn]
      [:div {:class "self-center flex flex-col md:flex-row items-center gap-4"}
       (cond
         @loading [components/spinner]
         @error [components/danger-alert
                 "Failed to retrieve the list of available queries.
                Please try reloading this page!"]
         :else [:<>
                [:label {:class "text-lg"} "Aggregation:"]
                (into [:select {:class ["py-2 px-4 pr-9 w-full"
                                        "border rounded-full border-gray-300 focus:border-blue-500"
                                        "text-sm"]
                                :on-change (fn [e]
                                             (let [v (-> e .-target .-value)]
                                               (selection-handler-fn v)))}
                       [:option {:defaultValue true
                                 :value ""}
                        "Select an aggregation..."]]

                      (for [{id :id desc :description} @queries]
                        [:option {:value id} desc]))])])))

(def ^:private chart-components {"scalar" components/scalar-chart
                                 "bar" components/bar-chart
                                 "pie" components/pie-chart})

(defn- aggregation-chart []
  (let [type (rf/subscribe [:fhir-quest.subs/agg-chart-type])
        data (rf/subscribe [:fhir-quest.subs/agg-chart-data])
        error (rf/subscribe [:fhir-quest.subs/agg-chart-error])
        loading (rf/subscribe [:fhir-quest.subs/agg-chart-loading])]
    (fn []
      [:div {:class "w-full flex flex-row justify-center"}
       (cond
         @loading [components/spinner]
         @error [components/danger-alert
                 "Failed to load chart data for this aggregation. Please try again!"]
         @type [:div {:class "max-w-xl w-full"}
                [(get chart-components @type) @data]])])))

(defn home []
  (rf/dispatch [:fhir-quest.events/fetch-aggregations])
  [:div {:class ["flex" "flex-col gap-12 md:gap-16"
                 "w-full max-w-4xl"
                 "mx-auto p-8 md:p-12"]}
   [:h1 {:class "text-3xl md:text-4xl"} "FHIR Quest"]
   [aggregation-selector #(rf/dispatch [:fhir-quest.events/selected-agg-id %])]
   [aggregation-chart]])
