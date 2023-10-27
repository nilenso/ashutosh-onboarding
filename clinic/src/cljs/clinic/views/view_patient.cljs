(ns clinic.views.view-patient
  (:require [ajax.core :as ajax]
            [clinic.components :as components]
            [clinic.views.not-found :as not-found]
            [re-frame.core :as rf]
            [reagent.core :as r]))


(rf/reg-event-db ::fetch-patient-success
                 (fn [db [_ result]]
                   (assoc db ::patient {::loading false ::data result})))

(rf/reg-event-db ::fetch-patient-failure
                 (fn [db [_ {error-code :status}]]
                   (assoc db ::patient {::loading false ::error-code error-code})))

(rf/reg-event-fx ::fetch-patient
                 (fn [{db :db} [_ {patient-id :id}]]
                   {:db (assoc-in db [::patient patient-id] {::loading true})
                    :http-xhrio {:method :get
                                 :uri (str "/api/v1/patients/" patient-id)
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::fetch-patient-success]
                                 :on-failure [::fetch-patient-failure]}}))

(rf/reg-sub ::patient get-in)

(defn- row []
  (into [:tr {:class ["border-b"]}]
        (r/children (r/current-component))))

(defn- cell []
  (let [this (r/current-component)
        props (r/props this)]
    (into [:td {:class (into ["px-6" "py-2"]
                             (get props :class []))}]
          (r/children this))))

(defn- marital-status-text [status]
  (case status
    "A" "Annulled"
    "D" "Divorced"
    "I" "Interlocutory"
    "L" "Legally Separated"
    "M" "Married"
    "P" "Polygamous"
    "S" "Never Married"
    "T" "Domestic partner"
    "U" "Unmarried"
    "W" "Widowed"
    "Unknown"))

(defn root []
  (let [loading? @(rf/subscribe [::patient ::loading])
        patient @(rf/subscribe [::patient ::data])
        error-code @(rf/subscribe [::patient ::error-code])]
    [:section {:class ["flex" "flex-col"]}
     (cond
       loading? [components/spinner {:class ["block" "self-center" "w-8" "h-8" "m-16" "text-blue-600"]}]
       (= 404 error-code) [not-found/root {:title "Patient Not Found"
                                           :message "This patient doesn't exist in our records!"}]
       error-code [components/danger-alert "There was an error while fetching patient data. Please try again!"]
       patient [:table {:class ["table-auto" "self-center"]}
                [:tbody
                 [row
                  [cell {:class ["text-gray-500"]} "Name"]
                  [cell (:first-name patient) " " (:last-name patient)]]
                 [row
                  [cell {:class ["text-gray-500"]} "Date of Birth"]
                  [cell (:birth-date patient)]]
                 [row
                  [cell {:class ["text-gray-500"]} "Gender"]
                  [cell {:class ["capitalize"]} (:gender patient)]]
                 [row
                  [cell {:class ["text-gray-500"]} "Phone"]
                  [cell (:phone patient)]]
                 [row
                  [cell {:class ["text-gray-500"]} "Email"]
                  [cell (:email patient)]]
                 [row
                  [cell {:class ["text-gray-500"]} "Marital Status"]
                  [cell (marital-status-text (:marital-status patient))]]]])]))
