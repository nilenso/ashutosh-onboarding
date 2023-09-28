(ns fhir-quest.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [fhir-quest.utils :as utils]
            [java-time.api :as jt]))

(deftest classifier-test
  (let [classify (utils/classifier {:group-a [0 10]
                                    :group-b [11 20]
                                    :group-c [21 30]}
                                   :unknown)]
    (testing "when value is within a group's range"
      (is (= :group-a (classify 0)))
      (is (= :group-b (classify 17)))
      (is (= :group-c (classify 30))))

    (testing "when value is not within a group's range"
      (is (= :unknown (classify -1))))))

(deftest count-by-test
  (testing "with identity function"
    (is (= {0 3
            1 3
            5 3}
           (utils/count-by identity [0 1 0 0 5 5 1 5 1]))))

  (testing "with custom grouping function"
    (is (= {:a 2
            :A 3}
           (utils/count-by :group
                           [{:group :a}
                            {:group :A}
                            {:group :A}
                            {:group :a}
                            {:group :A}])))))

(deftest months-since-test
  (jt/with-clock (jt/mock-clock (jt/instant "2000-01-01T00:00:00+05:30"))
    (testing "with dates before n months"
      (doseq [[date months-since] {"1999-12-01" 1
                                   "1999-10-01" 3
                                   "1999-07-01" 6
                                   "1999-04-01" 9
                                   "1999-01-01" 12}]
        (is (= months-since (utils/months-since date)))))

    (testing "with date after n months"
      (doseq [[date months-since] {"2000-02-01" -1
                                   "2000-04-01" -3
                                   "2000-07-01" -6
                                   "2000-10-01" -9
                                   "2001-01-01" -12}]
        (is (= months-since (utils/months-since date)))))))

(deftest average-test
  (testing "with no quantities"
    (is (= 0 (utils/average []))))

  (testing "with some quantities"
    (is (= 465/11
           (utils/average [35 21 66 70 76 6 26 59 73 15 18])))))
