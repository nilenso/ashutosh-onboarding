(ns clinic.service.patient
  (:require [clinic.fhir.client :as fc]
            [clinic.fhir.utils :as fu]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def mrn-system "urn:nilenso:clinic:mrn")
(def marital-status-system "http://hl7.org/fhir/ValueSet/marital-status")
(def email-system "email")
(def phone-system "phone")

(def ^:private not-blank? (complement string/blank?))

(s/def ::id (s/and string? not-blank?))
(s/def ::mrn (s/and string? not-blank?))
(s/def ::first-name (s/and string? not-blank?))
(s/def ::last-name (s/and string? not-blank?))
(s/def ::birth-date (s/and string? not-blank?))
(s/def ::gender #{"male" "female" "other" "unknown"})
(s/def ::marital-status #{nil, "A" "D" "I" "L" "M" "P" "S" "T" "U" "W" "UNK"})
(s/def ::email (s/nilable (s/and string? not-blank?)))
(s/def ::phone (s/nilable (s/and string? not-blank?)))

(s/def ::create-params
  (s/keys :req-un [::first-name ::last-name ::birth-date ::gender]
          :opt-un [::marital-status ::email ::phone]))

(s/def ::patient
  (s/keys :req-un [::id ::mrn ::first-name ::last-name ::birth-date ::gender]
          :opt-un [::marital-status ::email ::phone]))

(defn- generate-mrn []
  (String/format "%03d-%03d-%03d"
                 (into-array [(rand-int 999)
                              (rand-int 999)
                              (rand-int 999)])))

(defn- domain->fhir [params]
  (let [resource (atom {:resourceType "Patient"
                        :identifier [{:system mrn-system
                                      :value (params :mrn)}]
                        :name [{:family (params :last-name)
                                :given [(params :first-name)]}]
                        :birthDate (params :birth-date)
                        :gender (params :gender)
                        :maritalStatus {:coding [{:system marital-status-system
                                                  :code (or (get params :marital-status)
                                                            "UNK")}]}
                        :telecom []
                        :active true})]
    (when (params :email)
      (swap! resource update :telecom conj {:system email-system
                                            :value (params :email)}))
    (when (params :phone)
      (swap! resource update :telecom conj {:system phone-system
                                            :value (params :phone)}))
    @resource))

(defn- fhir->domain [resource]
  (let [entity (atom {:id (resource :id)
                      :mrn (->> resource
                                (:identifier)
                                (fu/find-value mrn-system))
                      :first-name (string/join " " (get-in resource [:name 0 :given]))
                      :last-name (get-in resource [:name 0 :family])
                      :birth-date (resource :birthDate)
                      :gender (resource :gender)
                      :marital-status (some->> resource
                                               (:maritalStatus)
                                               (fu/find-code marital-status-system))})
        email (some->> resource
                       (:telecom)
                       (fu/find-value email-system))
        phone (some->> resource
                       (:telecom)
                       (fu/find-value phone-system))]
    (when email
      (swap! entity assoc :email email))
    (when phone
      (swap! entity assoc :phone phone))
    (s/conform ::patient @entity)))

(defn create!
  "Creates a new patient resource using attributes of the given `params` and
   uses the FHIR server at the given `fhir-server-url` to persist these Patient
   resources."
  [fhir-server-url params]
  (when-not (s/valid? ::create-params params)
    (throw (ex-info "invalid create params"
                    {:type :invalid-params
                     :details (s/explain-data ::create-params params)})))
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
