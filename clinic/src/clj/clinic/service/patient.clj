(ns clinic.service.patient
  (:require [clinic.fhir.client :as fc]
            [clinic.fhir.utils :as fu]
            [clinic.specs.patient :as specs]
            [clinic.utils :as u]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def marital-status-system "http://hl7.org/fhir/ValueSet/marital-status")
(def email-system "email")
(def phone-system "phone")

(defn- domain->fhir [params]
  (cond-> {:resourceType "Patient"
           :name [{:family (params :last-name)
                   :given [(params :first-name)]}]
           :birthDate (params :birth-date)
           :gender (params :gender)
           :telecom []
           :active true}
    (params :marital-status) (assoc :maritalStatus
                                    {:coding [{:system marital-status-system
                                               :code (params :marital-status)}]})
    (params :email) (update :telecom conj {:system email-system
                                           :value (params :email)})
    (params :phone) (update :telecom conj {:system phone-system
                                           :value (params :phone)})))

(defn- fhir->domain [resource]
  (let [marital-status (some->> resource
                                (:maritalStatus)
                                (fu/find-code marital-status-system))
        email (some->> resource
                       (:telecom)
                       (fu/find-value email-system))
        phone (some->> resource
                       (:telecom)
                       (fu/find-value phone-system))
        entity (cond-> {:id (resource :id)
                        :first-name (string/join " " (get-in resource [:name 0 :given]))
                        :last-name (get-in resource [:name 0 :family])
                        :birth-date (resource :birthDate)
                        :gender (resource :gender)}
                 marital-status (assoc :marital-status marital-status)
                 email (assoc :email email)
                 phone (assoc :phone phone))]
    (s/conform ::specs/patient entity)))

(defn create!
  "Creates a new patient resource using attributes of the given `params` and
   uses the FHIR server at the given `fhir-server-url` to persist these Patient
   resources."
  [fhir-server-url params]
  (when-not (s/valid? ::specs/create-params params)
    (throw (ex-info "invalid create params"
                    {:type :invalid-params
                     :details (s/explain-data ::specs/create-params params)})))
  (let [{status :status
         body   :body} (fc/create! fhir-server-url
                                   (-> params
                                       ;; ignore phone number formatting
                                       ;; characters and only keep its digits.
                                       (update :phone u/extract-digits)
                                       (domain->fhir))
                                   nil)]
    (cond
      (= status 201) (fhir->domain body)
      :else (throw (ex-info "upstream service error"
                            {:type :upstream-error
                             :response {:status status :body body}})))))

(defn get-all
  "Lists patient resources and uses the given `params` to apply filters to the
   search. The accepted `params` are:

   - `:phone` (optional): The phone number of the Patient.
   - `:offset` (optional, default 0): The number of Patient resources to skip in
     the result set.
   - `:count` (optional, default 10): The maximum count of Patient resources to
     return with the result.
   "
  [fhir-server-url params]
  (when-not (s/valid? ::specs/get-all-params params)
    (throw (ex-info "invalid get-all params"
                    {:type :invalid-params
                     :details (s/explain-data ::specs/get-all-params params)})))
  (let [{:keys [phone offset count]} params
        query-params (cond-> {:_offset "0"
                              :_count "10"}
                       phone (assoc :phone (u/extract-digits phone))
                       offset (assoc :_offset offset)
                       count (assoc :_count count))
        {status :status
         body :body} (fc/get-all fhir-server-url "Patient" query-params)]
    (cond
      (= status 200) (->> body
                          (:entry)
                          (map :resource)
                          (map fhir->domain))
      :else (throw (ex-info "upstream service error"
                            {:type :upstream-error
                             :response {:status status :body body}})))))
