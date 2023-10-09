(ns clinic.fhir.utils-test
  (:require [clinic.fhir.utils :as fu]
            [clojure.test :refer [deftest is]]))

(deftest find-code-test
  (let [codeable-concept {:coding [{:system "test-system-1"
                                    :code "code-1"}
                                   {:system "test-system-2"
                                    :code "code-2"}
                                   {:system "test-system-3"
                                    :code "code-3"}]}]
    (is (= "code-1" (fu/find-code "test-system-1" codeable-concept)))
    (is (= "code-2" (fu/find-code "test-system-2" codeable-concept)))
    (is (= "code-3" (fu/find-code "test-system-3" codeable-concept)))))

(deftest find-value-test
  (let [elements [{:system "test-system-1"
                   :value "value-1"}
                  {:system "test-system-2"
                   :value "value-2"}
                  {:system "test-system-3"
                   :value "value-3"}]]
    (is (= "value-1" (fu/find-value "test-system-1" elements)))
    (is (= "value-2" (fu/find-value "test-system-2" elements)))
    (is (= "value-3" (fu/find-value "test-system-3" elements)))))
