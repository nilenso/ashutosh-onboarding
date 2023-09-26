(ns fhir-quest.pages
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [fhir-quest.components :as components]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::fetch-aggregations
                 (fn [{db :db} _]
                   {:db (assoc-in db [:agg-selector :loading] true)
                    :http-xhrio {:method :get
                                 :uri "/api/v1/aggregation"
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-queries-success]
                                 :on-failure [::fetch-queries-failure]}}))

(rf/reg-event-db ::fetch-queries-success
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:agg-selector :queries] result)
                       (assoc-in [:agg-selector :loading] false))))

(rf/reg-event-db ::fetch-queries-failure
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:agg-selector :error] result)
                       (assoc-in [:agg-selector :loading] false))))

(rf/reg-sub ::agg-selector-queries :-> #(get-in % [:agg-selector :queries]))
(rf/reg-sub ::agg-selector-error :-> #(get-in % [:agg-selector :error]))
(rf/reg-sub ::agg-selector-loading :-> #(get-in % [:agg-selector :loading]))

(defn- aggregation-selector [selection-handler-fn]
  (let [queries @(rf/subscribe [::agg-selector-queries])
        error @(rf/subscribe [::agg-selector-error])
        loading @(rf/subscribe [::agg-selector-loading])]
    [:div {:class ["self-center"
                   "flex flex-col md:flex-row items-center gap-4"]}
     (cond
       loading [components/spinner]
       error [components/danger-alert
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

                    (for [{id :id desc :description} queries]
                      [:option {:value id} desc]))])]))

(rf/reg-event-fx ::fetch-chart
                 (fn [{db :db} [_ agg-id]]
                   {:db (assoc-in db [:agg-chart :loading] true)
                    :http-xhrio {:method :get
                                 :uri (str "/api/v1/aggregation/" agg-id "/chart")
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-chart-success]
                                 :on-failure [::fetch-chart-failure]}}))

(rf/reg-event-db ::fetch-chart-success
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:agg-chart :chart] result)
                       (assoc-in [:agg-chart :loading] false))))

(rf/reg-event-db ::fetch-chart-failure
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:agg-chart :error] result)
                       (assoc-in [:agg-chart :loading] false))))

(rf/reg-sub ::agg-chart-type :-> #(get-in % [:agg-chart :chart :type]))
(rf/reg-sub ::agg-chart-data :-> #(get-in % [:agg-chart :chart :data]))
(rf/reg-sub ::agg-chart-error :-> #(get-in % [:agg-chart :error]))
(rf/reg-sub ::agg-chart-loading :-> #(get-in % [:agg-chart :loading]))

(def ^:private chart-components {"scalar" components/scalar-chart
                                 "bar" components/bar-chart
                                 "pie" components/pie-chart})

(defn- aggregation-chart []
  (let [type (rf/subscribe [::agg-chart-type])
        data (rf/subscribe [::agg-chart-data])
        error (rf/subscribe [::agg-chart-error])
        loading (rf/subscribe [::agg-chart-loading])]
    (fn []
      [:div {:class ["w-full flex flex-row justify-center"]}
       (cond
         @loading [components/spinner]
         @error [components/danger-alert
                 "Failed to load chart data for this aggregation. Please try again!"]
         @type [:div {:class ["max-w-xl w-full"]}
                [(get chart-components @type) @data]])])))

(rf/reg-event-fx ::selected-agg-id
                 (fn [{db :db} [_ agg-id]]
                   (if (empty? agg-id)
                     {:db (assoc db :agg-chart nil)}
                     {:db db
                      :dispatch [::fetch-chart agg-id]})))

(defn home []
  (rf/dispatch [::fetch-aggregations])
  [:div {:class ["flex" "flex-col gap-12 md:gap-16"
                 "w-full max-w-4xl"
                 "mx-auto p-8 md:p-12"]}
   [:h1 {:class ["text-3xl md:text-4xl"]} "FHIR Quest"]
   [aggregation-selector #(rf/dispatch [::selected-agg-id %])]
   [aggregation-chart]])
