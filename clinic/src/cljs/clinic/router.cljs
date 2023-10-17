(ns clinic.router
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [re-frame.core :as rf]))

(def ^:private routes
  ["/" {"" ::home
        "patients/" {"new" ::create-patient
                     [:id] ::view-patient}}])

(def ^:private history
  (pushy/pushy #(rf/dispatch [::set-current-view (:handler %)])
               (partial bidi/match-route routes)))

(defn start! []
  (pushy/start! history))

(defn replace-token! [token]
  (pushy/replace-token! history token))

(defn set-token! [token]
  (pushy/set-token! history token))

(rf/reg-fx ::set-token
           (fn router-set-token-effect [token]
             (set-token! token)))
