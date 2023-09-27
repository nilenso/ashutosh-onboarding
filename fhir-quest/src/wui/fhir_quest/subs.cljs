(ns fhir-quest.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub ::agg-selector-items :-> #(get-in % [:agg-selector :items]))
(rf/reg-sub ::agg-selector-error :-> #(get-in % [:agg-selector :error]))
(rf/reg-sub ::agg-selector-loading :-> #(get-in % [:agg-selector :loading]))

(rf/reg-sub ::agg-chart-type :-> #(get-in % [:agg-chart :chart :type]))
(rf/reg-sub ::agg-chart-data :-> #(get-in % [:agg-chart :chart :data]))
(rf/reg-sub ::agg-chart-error :-> #(get-in % [:agg-chart :error]))
(rf/reg-sub ::agg-chart-loading :-> #(get-in % [:agg-chart :loading]))
