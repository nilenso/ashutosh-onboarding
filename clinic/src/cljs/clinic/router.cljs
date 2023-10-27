(ns clinic.router
  (:require [bidi.bidi :as bidi]
            [clinic.utils :as u]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def ^:private routes
  ["" {"/" ::home
       "/patients" {"/new" ::create-patient
                    ["/" :id] ::view-patient
                    "" ::search-patients}}])

(def ^:private history
  (pushy/pushy #(rf/dispatch [::set-current-view
                              (:handler %)
                              (merge (:route-params %)
                                     (:query-params %))])
               #(-> (bidi/match-route routes %)
                    (assoc :query-params (u/query-params %)))))

(def path-for (partial bidi/path-for routes))

(defn start! []
  (pushy/start! history))

(rf/reg-event-fx ::set-current-view
                 (fn [{db :db} [_ view-id params]]
                   {:db (assoc db ::current-view {::id view-id ::params params})
                    :dispatch [::on-current-view-changed]}))

(rf/reg-sub ::current-view get-in)

(rf/reg-fx ::set-token
           (fn router-set-token-effect [token]
             (pushy/set-token! history token)))

(rf/reg-fx ::replace-token
           (fn router-replace-token-effect [token]
             (pushy/replace-token! history token)))

(rf/reg-event-fx ::set-token
                 (fn [_ [_ token]]
                   {::set-token token}))

(rf/reg-event-fx ::replace-token
                 (fn [_ [_ token]]
                   {::replace-token token}))
