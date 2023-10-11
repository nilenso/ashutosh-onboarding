(ns clinic.user-role.events
  (:require [clinic.user-role.effects :as effects]
            [re-frame.core :as rf]))

(rf/reg-event-fx ::set
                 (fn [_ [_ role]]
                   {::effects/set-cookie role
                    :dispatch [::get]}))

(rf/reg-event-fx ::get
                 [(rf/inject-cofx ::effects/get-cookie)]
                 (fn [{db :db role ::effects/get-cookie} _]
                   {:db (assoc db ::user-role role)}))
