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

(rf/reg-fx ::set-window-title
           (fn [title]
             (set! (.-title js/document)
                   (-> title
                       (or "Page Not Found")
                       (str " - Acme Clinic")))))

(rf/reg-event-fx ::router/on-current-view-changed
                 (fn [{db :db} _]
                   (let [{view-id ::router/id
                          params ::router/params} (::router/current-view db)]
                     (case view-id
                       ::router/home {::set-window-title "Home"}
                       ::router/create-patient {::set-window-title "Add Patient"}
                       ::router/view-patient {::set-window-title "Patient Info"
                                              :dispatch [::view-patient/fetch-patient params]}
                       ::router/search-patients {::set-window-title "Search Patients"
                                                 :dispatch [::list-patients/fetch-patients params]}))))

(defn root []
  (let [current-role @(user-role/get)
        current-view-id @(rf/subscribe [::router/current-view ::router/id])]
    [components/page {:title-href (router/path-for ::router/home)
                      :logout-enabled current-role
                      :on-logout-click #(do (user-role/clear)
                                            (rf/dispatch [::router/replace-token
                                                          (router/path-for ::router/home)]))}
     [(case current-view-id
        ::router/home home/root
        ::router/create-patient create-patient/root
        ::router/view-patient view-patient/root
        ::router/search-patients list-patients/root
        not-found/root)]]))
