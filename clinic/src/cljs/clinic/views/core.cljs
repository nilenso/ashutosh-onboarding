(ns clinic.views.core
  (:require [clinic.components :as components]
            [clinic.user-role.core :as user-role]
            [clinic.views.home :as home]
            [clinic.views.not-found :as not-found]
            [re-frame.core :as rf]))

(def ^:private views {::home home/root})
(def ^:private titles {::create-patient "Add Patient"})

(rf/reg-fx ::set-window-title
           (fn [view-id]
             (set! (.-title js/document) (or (some-> titles
                                                     (get view-id)
                                                     (str " - Clinic"))
                                             "Clinic"))))

(rf/reg-event-fx ::set-current-view
                 (fn [{db :db} [_ view-id]]
                   {:db (assoc db ::current-view-id view-id)
                    ::set-window-title view-id}))

(rf/reg-sub ::current-view :-> ::current-view-id)

(defn set-current [view-id]
  (rf/dispatch [::set-current-view view-id]))

(defn root []
  (let [current-role (user-role/get)
        current-view (rf/subscribe [::current-view])]
    (fn []
      [components/page {:logout-enabled @current-role
                        :on-logout-click #(do (user-role/clear)
                                              (js/window.location.replace "/"))}
       [(get views @current-view not-found/root)]])))
