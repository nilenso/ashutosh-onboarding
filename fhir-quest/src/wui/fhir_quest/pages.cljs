(ns fhir-quest.pages
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [fhir-quest.components :as components]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::fetch-queries
                 (fn [{db :db} _]
                   {:db (assoc-in db [:query-selector :loading] true)
                    :http-xhrio {:method :get
                                 :uri "/api/v1/query"
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-queries-success]
                                 :on-failure [::fetch-queries-failure]}}))

(rf/reg-event-db ::fetch-queries-success
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:query-selector :queries] result)
                       (assoc-in [:query-selector :loading] false))))

(rf/reg-event-db ::fetch-queries-failure
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:query-selector :error] result)
                       (assoc-in [:query-selector :loading] false))))

(rf/reg-sub ::query-selector-queries :-> #(get-in % [:query-selector :queries]))
(rf/reg-sub ::query-selector-error :-> #(get-in % [:query-selector :error]))
(rf/reg-sub ::query-selector-loading :-> #(get-in % [:query-selector :loading]))

(defn- query-selector [selection-handler-fn]
  (let [queries @(rf/subscribe [::query-selector-queries])
        error @(rf/subscribe [::query-selector-error])
        loading @(rf/subscribe [::query-selector-loading])]
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
                      "Select a query..."]]

                    (for [{id :id desc :description} queries]
                      [:option {:value id} desc]))])]))

(rf/reg-event-fx ::fetch-chart
                 (fn [{db :db} [_ query-id]]
                   {:db (assoc-in db [:query-chart :loading] true)
                    :http-xhrio {:method :get
                                 :uri (str "/api/v1/query/" query-id "/chart")
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-chart-success]
                                 :on-failure [::fetch-chart-failure]}}))

(rf/reg-event-db ::fetch-chart-success
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:query-chart :chart] result)
                       (assoc-in [:query-chart :loading] false))))

(rf/reg-event-db ::fetch-chart-failure
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:query-chart :error] result)
                       (assoc-in [:query-chart :loading] false))))

(rf/reg-sub ::query-chart-type :-> #(get-in % [:query-chart :chart :type]))
(rf/reg-sub ::query-chart-data :-> #(get-in % [:query-chart :chart :data]))
(rf/reg-sub ::query-chart-error :-> #(get-in % [:query-chart :error]))
(rf/reg-sub ::query-chart-loading :-> #(get-in % [:query-chart :loading]))

(def ^:private chart-components {"scalar" components/scalar-chart
                                 "bar" components/bar-chart
                                 "pie" components/pie-chart})

(defn- query-chart []
  (let [type (rf/subscribe [::query-chart-type])
        data (rf/subscribe [::query-chart-data])
        error (rf/subscribe [::query-chart-error])
        loading (rf/subscribe [::query-chart-loading])]
    (fn []
      [:div {:class ["max-w-xl w-full mx-auto"]}
       (cond
         @loading [components/spinner]
         @error [components/danger-alert
                 "Failed to load chart data for this query. Please try again!"]
         @type [(get chart-components @type) @data])])))

(rf/reg-event-fx ::selected-query-id
                 (fn [{db :db} [_ query-id]]
                   (if (empty? query-id)
                     {:db (assoc db :query-chart nil)}
                     {:db db
                      :dispatch [::fetch-chart query-id]})))

(defn home []
  (rf/dispatch [::fetch-queries])
  [:div {:class ["flex" "flex-col gap-12 md:gap-16"
                 "w-full max-w-4xl"
                 "mx-auto p-8 md:p-12"]}
   [:h1 {:class ["text-3xl md:text-4xl"]} "FHIR Quest"]
   [query-selector #(rf/dispatch [::selected-query-id %])]
   [query-chart]])
