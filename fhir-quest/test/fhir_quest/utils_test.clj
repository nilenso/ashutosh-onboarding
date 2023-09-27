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
           (utils/count-by #(get % :group)
                           [{:group :a}
                            {:group :A}
                            {:group :A}
                            {:group :a}
                            {:group :A}])))))

(defn- date-before [months]
  (-> (jt/local-date)
      (.minusMonths months)))

(defn- date-after [months]
  (-> (jt/local-date)
      (.plusMonths months)))

(deftest months-since-test
  (testing "with dates before n months"
    (is (= 1 (utils/months-since (date-before 1))))
    (is (= 3 (utils/months-since (date-before 3))))
    (is (= 6 (utils/months-since (date-before 6))))
    (is (= 9 (utils/months-since (date-before 9))))
    (is (= 12 (utils/months-since (date-before 12)))))

  (testing "with date after n months"
    (is (= -1 (utils/months-since (date-after 1))))
    (is (= -3 (utils/months-since (date-after 3))))
    (is (= -6 (utils/months-since (date-after 6))))
    (is (= -9 (utils/months-since (date-after 9))))
    (is (= -12 (utils/months-since (date-after 12))))))

(deftest average-test
  (testing "with no quantities"
    (is (= 0 (utils/average []))))

  (testing "with some quantities"
    (is (= 465/11
           (utils/average [35 21 66 70 76 6 26 59 73 15 18])))))
