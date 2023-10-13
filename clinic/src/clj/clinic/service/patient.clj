(ns clinic.service.patient
  (:require [clinic.fhir.client :as fc]
            [clinic.fhir.utils :as fu]
            [clinic.specs.patient :as specs]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def mrn-system "urn:nilenso:clinic:mrn")
(def marital-status-system "http://hl7.org/fhir/ValueSet/marital-status")
(def email-system "email")
(def phone-system "phone")

(defn generate-mrn []
  (String/format "%03d-%03d-%03d"
                 (into-array [(rand-int 999)
                              (rand-int 999)
                              (rand-int 999)])))

(defn- domain->fhir [params]
  (cond-> {:resourceType "Patient"
           :identifier [{:system mrn-system
                         :value (params :mrn)}]
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
                        :mrn (->> resource
                                  (:identifier)
                                  (fu/find-value mrn-system))
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
  (loop [retry-count 3]
    (let [mrn (generate-mrn)
          resource (-> params
                       (assoc :mrn mrn)
                       (domain->fhir))
          response (fc/create! fhir-server-url
                               resource
                               {"If-None-Exist" (str "identifier=" mrn-system "|" mrn)})
          {status :status body :body} response]
      (cond
        (= 0 retry-count) (throw (ex-info "couldn't generate a unique MRN"
                                          {:type :mrn-conflict}))
        (= status 200) (recur (dec retry-count)) ;; MRN conflict
        (= status 201) (fhir->domain body)
        :else (throw (ex-info "upstream service error"
                              {:type :upstream-error
                               :response {:status status :body body}}))))))
