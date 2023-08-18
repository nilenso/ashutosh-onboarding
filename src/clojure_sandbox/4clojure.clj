(ns clojure-sandbox.4clojure
  (:require [clojure.string]))

;; 21. Write a function which returns the Nth element from a sequence.
(defn nth-element [[one & others :as sequence] position]
  (if (seq sequence)
    (if (pos? position)
      (recur others (dec position))
      one)
    nil))

(nth-element [] 1)
(nth-element [0 1 2 3 4] 2)
(nth-element [0 1 2 3] 5)

;; 22. Write a function which returns the total number of elements in a sequence.
(defn count-total [init-sequence]
  (loop [c 0, s init-sequence]
    (if (seq s)
      (recur (inc c) (rest s))
      c)))

(count-total [])
(count-total [1 2 3])
(count-total [1 2 3 4 5])


;; 23. Write a function which reverses a sequence.
(defn reverse-sequence [sequence]
  (loop [s sequence, r []]
    (if (seq s)
      (recur (rest s) (cons (first s) r))
      r)))

(reverse-sequence [1 2 3 4])
(reverse-sequence [])

;; 24. Write a function which returns the sum of a sequence of numbers.
(defn sum [nums]
  (apply + nums))

(sum [1 2 3])
(sum [])

;; 25. Write a function which returns only the odd numbers from a sequence.
(defn filter-odd [nums]
  (loop [result [], [one & others :as n] nums]
    (if (seq n)
      (recur
       (if (== 0 (mod one 2))
         result
         (conj result one))
       others)
      result)))

(filter-odd [1 2 3 4 5 6])
(filter-odd [1 2 3 4 5 6 7])
(filter-odd [])

;; 26. Write a function which returns the first X fibonacci numbers.
(defn fibonacci-sequence [n]
  (cond
    (== n 0) []
    (== n 1) [1]
    :else (loop [x (- n 2) , result [1 1]]
            (if (pos? x)
              (recur (dec x) (conj result (apply + (take-last 2 result))))
              result))))

(fibonacci-sequence 0)
(fibonacci-sequence 1)
(fibonacci-sequence 2)
(fibonacci-sequence 7)

;; 27. Write a function which returns true if the given sequence is a palindrome.
(defn is-palindrome [s]
  (if (<= (count s) 1)
    true
    (let [left (first s)
          right (last s)
          others (butlast (rest s))]
      (and (= left right) (recur others)))))

(is-palindrome "abc")
(is-palindrome "aba")
(is-palindrome [1 1 1 2])
(is-palindrome [1 1 2 2 1 1])

;; 28. Write a function which flattens a sequence.
(defn flatten-list [[h & t :as s]]
  (if (seq s)
    (if (coll? h)
      (concat (flatten-list h) (flatten-list t))
      (cons h (flatten-list t)))
    '()))

(flatten-list '((1 2) 3 [4 [5 6]]))
(flatten-list ["a" ["b"] "c"])
(flatten-list '((((:a)))))

;; 29. Write a function which takes a string and returns a new string containing
;; only the capital letters.
(defn filter-capital-letters [[head & others :as s]]
  (if (seq s)
    (if (Character/isUpperCase head)
      (clojure.string/join [head (filter-capital-letters others)])
      (filter-capital-letters others))
    ""))

(filter-capital-letters "HeLlO, WoRlD!")
(filter-capital-letters "nothing")
(filter-capital-letters "$#A(*&987Zf")


;; 30. Write a function which removes consecutive duplicates from a sequence.
(defn remove-consecutive-duplicates [s]
  (loop [result []
         [current & remaining] s]
    (cond
      (nil? current) result
      (= current (last result)) (recur result remaining)
      :else (recur (conj result current) remaining))))


(apply str (remove-consecutive-duplicates "Leeeeeerrroyyy"))
(remove-consecutive-duplicates [1 1 2 3 3 2 2 3])
(remove-consecutive-duplicates [[1 2] [1 2] [3 4] [1 2]])
