(ns clinic.views.create-patient
  (:require [ajax.core :as ajax]
            [clinic.components :as components]
            [clinic.router :as router]
            [clinic.specs.patient :as specs]
            [clinic.utils :as u]
            [clojure.spec.alpha :as s]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(rf/reg-event-fx ::submit-form-success
                 (fn [{db :db} [_ result]]
                   {:db (assoc db ::submitting-form false)
                    ::router/set-token (str "/patients/" (result :id))}))

(rf/reg-event-db ::submit-form-failure
                 (fn [db [_ result]]
                   (into db {::submitting-form false
                             ::submit-form-error-code (result :status)})))

(rf/reg-event-fx ::submit-form
                 (fn [{db :db} [_ form-data]]
                   {:db (assoc db ::submitting-form true)
                    :http-xhrio {:method :post
                                 :uri "/api/v1/patients/"
                                 :params form-data
                                 :format (ajax/json-request-format)
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::submit-form-success]
                                 :on-failure [::submit-form-failure]}}))

(rf/reg-sub ::submitting-form :-> ::submitting-form)
(rf/reg-sub ::submit-form-error-code :-> ::submit-form-error-code)

(defn root []
  (let [form-valid? (r/atom nil)
        submitting? (rf/subscribe [::submitting-form])
        submit-error-code (rf/subscribe [::submit-form-error-code])]
    (fn []
      [:section {:class ["flex" "flex-col" "gap-12"]}
       [components/heading-2 "Add a Patient"]
       [:form {:method "POST"
               :action "/api/v1/patients/"
               :class ["w-full" "flex" "flex-col" "gap-4"]
               :on-submit #(do (.preventDefault %)
                               (let [form-data (-> %
                                                   (.-target)
                                                   (js/FormData.)
                                                   (u/form-data->map))]
                                 (->> form-data
                                      (s/valid? ::specs/create-params)
                                      (reset! form-valid?))

                                 (when @form-valid?
                                   (rf/dispatch [::submit-form form-data]))))}

        (when @submit-error-code
          [components/danger-alert
           (case @submit-error-code
             400 "Something doesn't seem right. Are you sure the form input is
                  correct?"
             "There was an error while adding patient. Please try again!")])

        (when (false? @form-valid?)
          [components/danger-alert "Missing required fields or invalid input!"])

        [:div {:class ["w-full" "flex" "flex-col" "md:flex-row" "gap-8" "md:gap-12"]}
         [components/text-field
          :first-name
          "First Name *"
          "Jane"
          "Please enter a valid first name!"
          (partial s/valid? ::specs/first-name)]

         [components/text-field
          :last-name
          "Last Name *"
          "Doe"
          "Please enter a valid last name!"
          (partial s/valid? ::specs/last-name)]]

        [:div {:class ["w-full" "flex" "flex-col" "md:flex-row" "gap-8" "md:gap-12"]}
         [components/text-field
          :birth-date
          "Date of Birth *"
          "1999-12-30"
          "Please enter a valid date of birth in YYYY-MM-DD format!"
          (partial s/valid? ::specs/birth-date)]

         [components/select-field
          :gender
          "Gender *"
          "unknown"
          [["Male" "male"]
           ["Female" "female"]
           ["Other" "other"]
           ["Unknown" "unknown"]]]]

        [components/select-field
         :marital-status
         "Marital Status"
         "UNK"
         [["Single" "S"]
          ["Divorced" "D"]
          ["Married" "M"]
          ["Widowed" "W"]
          ["Unknown" "UNK"]]]

        [components/text-field
         :email
         "Email"
         "jane@doe.org"
         "Please enter a valid email!"
         (partial s/valid? ::specs/email)]

        [components/text-field
         :phone
         "Phone"
         "0000-000-000"
         "Please enter a valid phone!"
         (partial s/valid? ::specs/phone)]

        [components/button "submit" "Add Patient" @submitting?]]])))
