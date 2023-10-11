(ns clinic.user-role.subs
  (:require [clinic.user-role.events :as events]
            [re-frame.core :as rf]))

(rf/reg-sub ::current-role :-> ::events/user-role)
