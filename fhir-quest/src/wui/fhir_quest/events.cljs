(ns fhir-quest.events
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
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
                       (assoc-in [:agg-selector :items] result)
                       (assoc-in [:agg-selector :loading] false))))

(rf/reg-event-db ::fetch-queries-failure
                 (fn [db [_ result]]
                   (-> db
                       (assoc-in [:agg-selector :error] result)
                       (assoc-in [:agg-selector :loading] false))))

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

(rf/reg-event-fx ::selected-agg-id
                 (fn [{db :db} [_ agg-id]]
                   (if (empty? agg-id)
                     {:db (assoc db :agg-chart nil)}
                     {:db db
                      :dispatch [::fetch-chart agg-id]})))
