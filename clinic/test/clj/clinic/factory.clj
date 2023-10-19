(ns clinic.factory
  (:require [clinic.specs.patient :as specs]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import java.time.LocalDate))

(defn- rand-date []
  (->> 30000 ; ~ 82 years
       (rand-int)
       (.minusDays (LocalDate/now))
       (.toString)))

(defn rand-phone []
  ;; the goal here is NOT to generate a phone number that conforms to a
  ;; national/international formatting standard. We just need seemingly valid
  ;; phone numbers for all intents and purposes.
  (String/format (rand-nth ["(%03d) %03d-%04d"
                            "%03d-%03d-%04d"
                            "+01 %03d %03d %04d"
                            "%03d%03d%04d"])
                 (into-array [(rand-int 999)
                              (rand-int 999)
                              (rand-int 9999)])))

(defn- rand-int-str
  ([] (rand-int-str 0 Integer/MAX_VALUE))
  ([start end] (String/format "%d" (into-array [(->> start
                                                     (- end)
                                                     (rand-int)
                                                     (+ start))]))))

(defn- with-generating-fn [gen-fn]
  (-> (fn [& _] (gen-fn))
      (gen/fmap (gen/return nil))
      (constantly)))

(defn- generate
  ([spec] (generate spec {}))
  ([spec overrides]
   (-> spec
       (s/gen overrides)
       (gen/generate))))

(defn create-params [& {:as overrides}]
  (merge (generate ::specs/create-params
                   {::specs/birth-date (with-generating-fn rand-date)
                    ::specs/phone (with-generating-fn rand-phone)})
         overrides))

(defn get-all-params [& {:as overrides}]
  (merge (generate ::specs/get-all-params
                   {::specs/offset (with-generating-fn rand-int-str)
                    ::specs/count (with-generating-fn #(rand-int-str 1 21))
                    ::specs/phone (with-generating-fn rand-phone)})
         overrides))


(defn fhir-patient [& {:keys [phone]}]
  {:resourceType "Patient"
   :name [{:family (generate ::specs/last-name)
           :given [(generate ::specs/first-name)]}]
   :birthDate (rand-date)
   :gender (generate ::specs/gender)
   :telecom [{:system "email"
              :value (generate ::specs/email)}
             {:system "phone"
              :value (or phone
                         (String/format "%010d"
                                        (into-array [(rand-int Integer/MAX_VALUE)])))}]
   :maritalStatus {:coding [{:system "http://hl7.org/fhir/ValueSet/marital-status"
                             :code (generate ::specs/marital-status)}]}

   :active true})
