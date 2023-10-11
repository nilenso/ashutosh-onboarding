(ns clinic.user-role.core
  (:refer-clojure :exclude [get set])
  (:require [clinic.user-role.events :as events]
            [clinic.user-role.subs :as subs]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))

(s/def ::role #{"doctor" "nurse" "patient"})

(defn set [role]
  (when-not (s/valid? ::role role)
    (throw (js/Error (str "invalid role: " role))))
  (rf/dispatch [::events/set role]))

(defn get []
  (rf/dispatch-sync [::events/get]) ; ensure that value is present in app state.
  (rf/subscribe [::subs/current-role]))
