(ns fhir-quest.service
  (:require [fhir-quest.repository :as repo]))

(defn list-aggregations [db-conn]
  (-> db-conn
      (repo/list-aggregations 0 -1)
      ((partial map #(select-keys % [:id :description])))))

(defn get-aggregation-chart [db-conn agg-id]
  (let [{:keys [chart_type data]} (repo/get-aggregation db-conn agg-id)]
    (when (and chart_type data)
      {:type chart_type
       :data data})))
