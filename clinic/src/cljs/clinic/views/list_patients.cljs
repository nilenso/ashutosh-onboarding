(ns clinic.views.list-patients
  (:require [ajax.core :as ajax]
            [clinic.components :as components]
            [clinic.router :as router]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(rf/reg-event-db ::fetch-patients-success
                 (fn [db [_ phone page result]]
                   (assoc-in db [::patients phone page] {::loading false
                                                         ::data result})))

(rf/reg-event-db ::fetch-patients-failure
                 (fn [db [_ phone page {error-code :status}]]
                   (assoc-in db [::patients phone page] {::loading false
                                                         ::error-code error-code})))

(rf/reg-event-fx ::fetch-patients
                 (fn [{db :db} [_ phone page]]
                   {:db (assoc-in db [::patients phone page] {::loading true})
                    :http-xhrio {:method :get
                                 :uri (str "/api/v1/patients/")
                                 :params (cond-> {:count 10
                                                  :offset (* 10 (dec page))}
                                           phone (assoc :phone phone))
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-patients-success phone page]
                                 :on-failure [::fetch-patients-failure phone page]}}))

(rf/reg-sub ::patients get-in)

(defn- patient-row [index patient]
  [:tr {:class ["hover:bg-gray-100" "hover:cursor-pointer"]
        :on-click #(router/set-token! (str "/patients/" (:id patient)))}
   [:td {:class ["px-6" "py-2"]} (inc index)]
   [:td {:class ["px-6" "py-2"]} (:first-name patient) " " (:last-name patient)]
   [:td {:class ["px-6" "py-2"]} (:birth-date patient)]
   [:td {:class ["px-6" "py-2"]} (:phone patient)]])

(defn root []
  (let [props (r/props (r/current-component))
        page (parse-long (get props :page "1"))
        phone (:phone props)
        loading? (rf/subscribe [::patients phone page ::loading])
        patients (rf/subscribe [::patients phone page ::data])
        error-code (rf/subscribe [::patients phone page ::error-code])]
    (rf/dispatch [::fetch-patients phone page])
    [:section {:class ["flex" "flex-col" "gap-8"]}
     (cond
       @loading?
       [components/spinner {:class ["block" "self-center" "w-8" "h-8" "m-16" "text-blue-600"]}]

       @error-code
       [components/danger-alert "There was an error while fetching patient data. Please try again!"]

       (empty? @patients)
       [:p {:class ["self-center" "text-center"]} "No patients found matching this criteria!"]

       @patients
       [:<>
        [:table {:class ["table-auto" "self-center" "text-center"]}
         [:thead
          [:tr {:class ["border-b"]}
           [:th {:class ["px-6" "py-2"]} "#"]
           [:th {:class ["px-6" "py-2"]} "Name"]
           [:th {:class ["px-6" "py-2"]} "Date of Birth"]
           [:th {:class ["px-6" "py-2"]} "Phone Number"]]]
         (into [:tbody] (map-indexed patient-row @patients))]
        [:div {:class ["flex" "flex-row" "justify-center" "gap-8"]}
         [:a {:class ["text-blue-600" "hover:underline"
                      (when (<= page 1) "invisible")]
              :href (cond-> (str "/patients?page=" (dec page))
                      phone (str "&phone=" phone))}
          "Prev"]
         [:p {:class ["font-medium"]} "Page" " " page]
         [:a {:class ["text-blue-600" "hover:underline"
                      (when (< (count @patients) 10) "invisible")]
              :href (cond-> (str "/patients?page=" (inc page))
                      phone (str "&phone=" phone))}
          "Next"]]])]))