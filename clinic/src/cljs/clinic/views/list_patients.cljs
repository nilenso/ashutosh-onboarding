(ns clinic.views.list-patients
  (:require [ajax.core :as ajax]
            [clinic.components :as components]
            [clinic.router :as router]
            [clinic.utils :as u]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(rf/reg-event-db ::fetch-patients-success
                 (fn [db [_ result]]
                   (assoc db ::patients {::loading false ::data result})))

(rf/reg-event-db ::fetch-patients-failure
                 (fn [db [_ {error-code :status}]]
                   (assoc db ::patients {::loading false ::error-code error-code})))

(rf/reg-event-fx ::fetch-patients
                 (fn [{db :db} [_ {:keys [phone page] :or {page "1"}}]]
                   (let [page-num (parse-long page)]
                     {:db (assoc db ::patients {::loading true})
                      :http-xhrio {:method :get
                                   :uri (str "/api/v1/patients/")
                                   :params (cond-> {:count 10
                                                    :offset (* 10 (dec page-num))}
                                             phone (assoc :phone phone))
                                   :response-format (ajax/json-response-format {:keywords? true})
                                   :on-success [::fetch-patients-success]
                                   :on-failure [::fetch-patients-failure]}})))

(rf/reg-sub ::patients get-in)

(defn- patient-row []
  (let [{:keys [index patient]} (r/props (r/current-component))]
    [:tr {:class [(if (odd? index) "bg-gray-50" "bg-white")
                  "hover:bg-gray-100" "hover:cursor-pointer"]
          :on-click #(rf/dispatch [::router/set-token
                                   (router/path-for ::router/view-patient :id (:id patient))])}
     [:td {:class ["px-6" "py-2"]} (inc index)]
     [:td {:class ["px-6" "py-2"]} (:first-name patient) " " (:last-name patient)]
     [:td {:class ["px-6" "py-2"]} (:birth-date patient)]
     [:td {:class ["px-6" "py-2"]} (:phone patient)]]))

(defn root []
  (let [params @(rf/subscribe [::router/current-view ::router/params])
        page (parse-long (get params :page "1"))
        phone (:phone params)
        loading? @(rf/subscribe [::patients ::loading])
        patients @(rf/subscribe [::patients ::data])
        error-code @(rf/subscribe [::patients ::error-code])]
    [:section {:class ["flex" "flex-col" "gap-8"]}
     [components/heading-2 "Search Patients"]
     [:form {:class ["flex" "flex-row" "self-center" "items-center" "gap-6"]
             :on-submit #(do (.preventDefault %)
                             (let [phone (-> js/document
                                             (.getElementById "phone")
                                             (.-value))]
                               (when-not (empty? phone)
                                 (rf/dispatch [::router/set-token
                                               (-> ::router/search-patients
                                                   (router/path-for)
                                                   (u/url {:phone phone}))]))))}
      [:input {:id "phone"
               :name "phone"
               :placeholder "Search by phone"
               :defaultValue phone
               :class ["appearance-none" "block" "w-full" "bg-gray-200"
                       "text-gray-700" "border" "border-gray-200"
                       "rounded" "py-2.5" "px-4" "leading-tight"
                       "focus:outline-none" "focus:bg-white"
                       "focus:border-gray-500"]}]
      [:input {:type "submit"
               :value "Search"
               :class ["bg-blue-600" "hover:bg-blue-800" "text-white"
                       "font-bold" "py-2" "px-4" "rounded-full"]}]]

     (cond
       loading?
       [components/spinner {:class ["block" "self-center" "w-8" "h-8" "m-16" "text-blue-600"]}]

       error-code
       [components/danger-alert "There was an error while fetching patient data. Please try again!"]

       (empty? patients)
       [:p {:class ["self-center" "text-center"]} "No patients found matching this criteria!"]

       patients
       [:<>
        [:table {:class ["w-full" "table-auto" "self-center" "text-center"]}
         [:thead
          [:tr {:class ["border-b"]}
           [:th {:class ["px-6" "py-2"]} "#"]
           [:th {:class ["px-6" "py-2"]} "Name"]
           [:th {:class ["px-6" "py-2"]} "Date of Birth"]
           [:th {:class ["px-6" "py-2"]} "Phone Number"]]]
         (into [:tbody] (map-indexed #(do [patient-row {:index %1
                                                        :patient %2}])
                                     patients))]
        [:div {:class ["flex" "flex-row" "justify-center" "gap-8"]}
         [:a {:class ["text-blue-600" "hover:underline"
                      (when (<= page 1) "invisible")]
              :href (-> (router/path-for ::search-patients)
                        (u/url (cond-> {:page (dec page)}
                                 phone (assoc :phone phone))))}
          "Prev"]
         [:p {:class ["font-medium"]} "Page" " " page]
         [:a {:class ["text-blue-600" "hover:underline"
                      (when (< (count patients) 10) "invisible")]
              :href (-> (router/path-for ::search-patients)
                        (u/url (cond-> {:page (inc page)}
                                 phone (assoc :phone phone))))}
          "Next"]]])]))
