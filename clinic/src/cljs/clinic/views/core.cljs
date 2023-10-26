(ns clinic.views.core
  (:require [clinic.components :as components]
            [clinic.router :as router]
            [clinic.user-role.core :as user-role]
            [clinic.views.create-patient :as create-patient]
            [clinic.views.home :as home]
            [clinic.views.list-patients :as list-patients]
            [clinic.views.not-found :as not-found]
            [clinic.views.view-patient :as view-patient]
            [re-frame.core :as rf]))

(def ^:private views {::router/home home/root
                      ::router/create-patient create-patient/root
                      ::router/view-patient view-patient/root
                      ::router/search-patients list-patients/root})

(def ^:private titles {::router/home "Home"
                       ::router/create-patient "Add Patient"
                       ::router/view-patient "Patient Info"
                       ::router/search-patients "Search Patients"})

(rf/reg-fx ::set-window-title
           (fn [title]
             (set! (.-title js/document)
                   (-> title
                       (or "Page Not Found")
                       (str " - Acme Clinic")))))

(rf/reg-event-fx ::router/set-current-view
                 (fn [{db :db} [_ view-id params]]
                   {:db (assoc db ::current-view {::id view-id ::params params})
                    ::set-window-title (titles view-id)}))

(rf/reg-sub ::current-view :-> ::current-view)

(defn root []
  (let [current-role (user-role/get)
        current-view (rf/subscribe [::current-view])]
    (fn []
      [components/page {:logout-enabled @current-role
                        :on-logout-click #(do (user-role/clear)
                                              (router/replace-token! "/"))}
       [(get views (::id @current-view) not-found/root)
        (::params @current-view)]])))
