(ns clinic.views.core
  (:require [clinic.components :as components]
            [clinic.router :as router]
            [clinic.user-role.core :as user-role]
            [clinic.views.create-patient :as create-patient]
            [clinic.views.home :as home]
            [clinic.views.not-found :as not-found]
            [re-frame.core :as rf]))

(def ^:private views {::router/home home/root
                      ::router/create-patient create-patient/root})

(def ^:private titles {::router/home "Home"
                       ::router/create-patient "Add Patient"})

(rf/reg-fx ::set-window-title
           (fn [view-id]
             (set! (.-title js/document)
                   (-> titles
                       (get view-id "Page Not Found")
                       (str " - Acme Clinic")))))

(rf/reg-event-fx ::router/set-current-view
                 (fn [{db :db} [_ view-id]]
                   {:db (assoc db ::current-view-id view-id)
                    ::set-window-title view-id}))

(rf/reg-sub ::current-view-id :-> ::current-view-id)

(defn root []
  (let [current-role (user-role/get)
        current-view (rf/subscribe [::current-view-id])]
    (fn []
      (println @current-view)
      [components/page {:logout-enabled @current-role
                        :on-logout-click #(do (user-role/clear)
                                              (router/replace-token! "/"))}
       [(get views @current-view not-found/root)]])))
