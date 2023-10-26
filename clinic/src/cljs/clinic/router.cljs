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

(defn start! []
  (pushy/start! history))

(defn replace-token! [token]
  (pushy/replace-token! history token))

(defn set-token! [token]
  (pushy/set-token! history token))

(rf/reg-fx ::set-token
           (fn router-set-token-effect [token]
             (set-token! token)))
