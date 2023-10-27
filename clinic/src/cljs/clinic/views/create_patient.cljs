(ns clinic.views.create-patient
  (:require [ajax.core :as ajax]
            [clinic.components :as components]
            [clinic.router :as router]
            [clinic.specs.patient :as specs]
            [clinic.utils :as u]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(rf/reg-event-fx ::submit-form-data-success
                 (fn [{db :db} [_ result]]
                   {:db (assoc db ::submit-form {::loading false})
                    ::router/set-token (router/path-for ::router/view-patient :id (result :id))}))

(rf/reg-event-db ::submit-form-data-failure
                 (fn [db [_ {error-code :status}]]
                   (assoc db ::submit-form {::loading false
                                            ::error-code error-code})))

(rf/reg-event-fx ::submit-form-data
                 (fn [{db :db} [_ form-data]]
                   {:db (assoc-in db [::submit-form ::loading] true)
                    :http-xhrio {:method :post
                                 :uri "/api/v1/patients/"
                                 :params form-data
                                 :format (ajax/json-request-format)
                                 :response-format (ajax/json-response-format {:keywords? true})
                                 :on-success [::submit-form-data-success]
                                 :on-failure [::submit-form-data-failure]}}))

(rf/reg-sub ::submit-form get-in)

(defn form-data [form]
  (-> form
      (js/FormData.)
      (u/form-data->map #{:marital-status :email})))

(defn find-invalid-keys [form]
  (->> form
       (form-data)
       (u/invalid-keys ::specs/create-params)))

(defn root []
  (let [form-ref (atom nil)
        touched? (r/atom #{})
        invalid? (r/atom #{})
        loading? (rf/subscribe [::submit-form ::loading])
        error-code (rf/subscribe [::submit-form ::error-code])]
    (fn []
      [:section {:class ["flex" "flex-col" "gap-12"]}
       [components/heading-2 "Add a Patient"]
       [:form {:ref (partial reset! form-ref)
               :method "POST"
               :class ["w-full" "flex" "flex-col" "gap-4"]
               :on-blur #(do (swap! touched? conj (-> %
                                                      (.-target)
                                                      (.-id)
                                                      (keyword)))
                             (reset! invalid? (find-invalid-keys @form-ref)))
               :on-change #(do (swap! touched? conj (-> %
                                                        (.-target)
                                                        (.-id)
                                                        (keyword)))
                               (reset! invalid? (find-invalid-keys @form-ref)))
               :on-submit #(do (.preventDefault %)
                               (let [form-data (form-data @form-ref)]
                                 ;; touch all fields and revalidate data.
                                 (reset! touched? (set (keys form-data)))
                                 (reset! invalid? (find-invalid-keys @form-ref))
                                 (when (empty? @invalid?)
                                   (rf/dispatch [::submit-form-data form-data]))))}

        (when @error-code
          [components/danger-alert
           (case @error-code
             400 "Something doesn't seem right. Are you sure the form input is
                  correct?"
             "There was an error while adding patient. Please try again!")])

        [:div {:class ["w-full" "flex" "flex-col" "md:flex-row" "gap-8" "md:gap-12"]}
         [components/text-field {:name :first-name
                                 :label "First Name *"
                                 :placeholder "Jane"
                                 :error-msg "Please enter a first name!"
                                 :touched? (contains? @touched? :first-name)
                                 :invalid? (contains? @invalid? :first-name)}]

         [components/text-field {:name :last-name
                                 :label "Last Name *"
                                 :placeholder "Doe"
                                 :error-msg "Please enter a last name!"
                                 :touched? (contains? @touched? :last-name)
                                 :invalid? (contains? @invalid? :last-name)}]]

        [:div {:class ["w-full" "flex" "flex-col" "md:flex-row" "gap-8" "md:gap-12"]}
         [components/text-field {:name :birth-date
                                 :label "Date of Birth *"
                                 :placeholder "1999-12-30"
                                 :error-msg "Please enter a date in YYYY-MM-DD format!"
                                 :touched? (contains? @touched? :birth-date)
                                 :invalid? (contains? @invalid? :birth-date)}]

         [components/select-field {:name :gender
                                   :label "Gender *"
                                   :default-value "unknown"
                                   :options [["Male" "male"]
                                             ["Female" "female"]
                                             ["Other" "other"]
                                             ["Unknown" "unknown"]]}]]

        [components/text-field {:name :phone
                                :label "Phone *"
                                :placeholder "+0 0000-000-000"
                                :error-msg "Please enter a phone number!"
                                :touched? (contains? @touched? :phone)
                                :invalid? (contains? @invalid? :phone)}]

        [components/text-field {:name :email
                                :label "Email"
                                :placeholder "jane@doe.org"
                                :error-msg "Please enter an email address!"
                                :touched? (contains? @touched? :email)
                                :invalid? (contains? @invalid? :email)}]

        [components/select-field {:name :marital-status
                                  :label "Marital Status"
                                  :default-value "UNK"
                                  :options [["Single" "S"]
                                            ["Divorced" "D"]
                                            ["Married" "M"]
                                            ["Widowed" "W"]
                                            ["Unknown" "UNK"]]}]

        [:div {:class ["h-4"]}]
        [components/button {:type "submit"
                            :text "Add Patient"
                            :loading? @loading?}]]])))
