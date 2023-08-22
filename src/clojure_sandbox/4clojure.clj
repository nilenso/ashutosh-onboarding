(ns clojure-sandbox.4clojure
  (:require [clojure.string :as str]))

;; 21. Write a function which returns the Nth element from a sequence.
(defn nth-element [[current & remaining :as items] position]
  (when items
    (if (pos? position)
      (recur remaining (dec position))
      current)))

(nth-element [] 1)
(nth-element [nil 1 2 3 4] 2)
(nth-element [0 1 2 3] 5)

;; 22. Write a function which returns the total number of elements in a sequence.
(defn count-total [init-sequence]
  (loop [count 0, remaining init-sequence]
    (if (seq remaining)
      (recur (inc count) (rest remaining))
      count)))

(count-total [])
(count-total [1 2 3])
(count-total [1 2 3 4 5])


;; 23. Write a function which reverses a sequence.
(defn reverse-sequence [init-sequence]
  (loop [remaining init-sequence, reversed []]
    (if (seq remaining)
      (recur (rest remaining) (cons (first remaining) reversed))
      reversed)))

(reverse-sequence [1 2 3 4])
(reverse-sequence [])

;; 24. Write a function which returns the sum of a sequence of numbers.
(defn sum [nums]
  (reduce + nums))

(sum [1 2 3])
(sum [])

;; 25. Write a function which returns only the odd numbers from a sequence.
(defn filter-odd [nums]
  (loop [odds [], [current & remaining :as nums] nums]
    (if (seq nums)
      (recur
       (if (== 0 (mod current 2))
         odds
         (conj odds current))
       remaining)
      odds)))

(filter-odd [1 2 3 4 5 6])
(filter-odd [1 2 3 4 5 6 7])
(filter-odd [])

;; 26. Write a function which returns the first X fibonacci numbers.
(defn fibonacci-sequence [count]
  (cond
    (== count 0) []
    (== count 1) [1]
    :else (loop [x (- count 2) , fib-nums [1 1]]
            (if (pos? x)
              (recur (dec x) (conj
                              fib-nums
                              (reduce + (take-last 2 fib-nums))))
              fib-nums))))

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
          remaining (butlast (rest s))]
      (and (= left right) (recur remaining)))))

(is-palindrome "abc")
(is-palindrome "aba")
(is-palindrome [1 1 1 2])
(is-palindrome [1 1 2 2 1 1])

;; 28. Write a function which flattens a sequence.
(defn flatten-list [[current & remaining :as items]]
  (when (seq items)
    (if (coll? current)
      (concat (flatten-list current) (flatten-list remaining))
      (cons current (flatten-list remaining)))))

(flatten-list '((1 2) 3 [4 [5 6]]))
(flatten-list ["a" ["b"] "c"])
(flatten-list '((((:a)))))

;; 29. Write a function which takes a string and returns a new string containing
;; only the capital letters.
(defn filter-capital-letters [[current & remaining :as s]]
  (if (seq s)
    (if (Character/isUpperCase current)
      (str/join [current (filter-capital-letters remaining)])
      (filter-capital-letters remaining))
    ""))

(filter-capital-letters "HeLlO, WoRlD!")
(filter-capital-letters "nothing")
(filter-capital-letters "$#A(*&987Zf")


;; 30. Write a function which removes consecutive duplicates from a sequence.
(defn remove-consecutive-duplicates [items]
  (reduce
   (fn [result current]
     (if (= current (last result))
       result
       (conj result current)))
   []
   items))

;; a more verbose approach
;; (defn remove-consecutive-duplicates [items]
;;   (loop [result []
;;          [current & remaining] items]
;;     (cond
;;       (nil? current) result
;;       (= current (last result)) (recur result remaining)
;;       :else (recur (conj result current) remaining))))


(apply str (remove-consecutive-duplicates "Leeeeeerrroyyy"))
(remove-consecutive-duplicates [1 1 2 3 3 2 2 3])
(remove-consecutive-duplicates [[1 2] [1 2] [3 4] [1 2]])


;; 31. Write a function which packs consecutive duplicates into sub-lists.

;; another approach using recursion.
;; (defn pack-sequence [[current & remaining]]
;;   (loop [packed-seq []
;;          curr-subseq [current]
;;          [current & remaining] remaining]
;;     (cond
;;       (nil? current) (conj packed-seq curr-subseq)
;;       (= current (first curr-subseq)) (recur
;;                                        packed-seq
;;                                        (conj curr-subseq current)
;;                                        remaining)
;;       :else (recur
;;              (conj packed-seq curr-subseq)
;;              [current]
;;              remaining))))

(defn pack-sequence [items]
  (reduce
   (fn [packed-seq current]
     (let [last-sublist (last packed-seq)]
       (if (= current (first last-sublist))
         (conj (subvec packed-seq
                       0
                       (dec (count packed-seq)))
               (conj last-sublist current))
         (conj packed-seq [current]))))
   []
   items))

(pack-sequence [1 1 2 1 1 1 3 3])
(pack-sequence [:a :a :b :b :c])
(pack-sequence [[1 2] [1 2] [3 4]])

;; 32. Write a function which duplicates each element of a sequence.
(defn duplicate-elements [items]
  (reduce #(conj %1 %2 %2) [] items))

(duplicate-elements [1 2 3])
(duplicate-elements [:a :a :b :b])
(duplicate-elements [[1 2] [3 4]])
(duplicate-elements [44 33])

;; 33. Write a function which replicates each element of a sequence a variable
;; number of times.
(defn replicate-n-times [items times]
  (reduce #(concat %1 (take times (repeat %2))) '() items))

(replicate-n-times [1 2 3] 2)
(replicate-n-times [:a :b] 4)
(replicate-n-times [4 5 6] 1)
(replicate-n-times [[1 2] [3 4]] 2)
(replicate-n-times [44 33] 2)

;; 34. Write a function which creates a list of all integers in a given range.
(defn range* [start end]
  (loop [current start, items []]
    (if (< current end)
      (recur (inc current) (conj items current))
      (seq items))))

(range* 1 4)
(range* -2 2)
(range* 5 8)

;; 35-37 on 4Clojure.

;; 38. Write a function which takes a variable number of parameters and returns
;; the maximum value.
(defn max* [& items]
  (reduce #(if (< %1 %2) %2 %1) items))

(max* 1 8 3 4)
(max* 30 20)
(max* 45 67 11)
