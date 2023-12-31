(ns clojure-sandbox.4clojure
  (:require [clojure.string :as string :only [join]]
            [clj-fuzzy.metrics :as m :only [levenshtein]]))

;; 21. Write a function which returns the Nth element from a sequence.
(defn nth-element [[current & remaining :as items] position]
  (when items
    (if (pos? position)
      (recur remaining (dec position))
      current)))

(comment (nth-element [] 1))
(comment (nth-element [nil 1 2 3 4] 2))
(comment (nth-element [0 1 2 3] 5))

;; 22. Write a function which returns the total number of elements in a sequence.
(defn count-total [s]
  (loop [count 0, remaining s]
    (if (seq remaining)
      (recur (inc count) (rest remaining))
      count)))

(comment (count-total []))
(comment (count-total [1 2 3]))
(comment (count-total [1 2 3 4 5]))


;; 23. Write a function which reverses a sequence.
(defn reverse-sequence [s]
  (loop [remaining s
         reversed []]
    (if (seq remaining)
      (recur (rest remaining) (cons (first remaining) reversed))
      reversed)))

(comment (reverse-sequence [1 2 3 4]))
(comment (reverse-sequence []))

;; 24. Write a function which returns the sum of a sequence of numbers.
(defn sum [nums]
  (reduce + nums))

(comment (sum [1 2 3]))
(comment (sum []))

;; 25. Write a function which returns only the odd numbers from a sequence.
(defn filter-odd [nums]
  (loop [odds []
         [current & remaining :as nums] nums]
    (if (seq nums)
      (recur
       (if (zero? (mod current 2))
         odds
         (conj odds current))
       remaining)
      odds)))

(comment (filter-odd [1 2 3 4 5 6]))
(comment (filter-odd [1 2 3 4 5 6 7]))
(comment (filter-odd []))

;; 26. Write a function which returns the first X fibonacci numbers.
(defn fibonacci-sequence [count]
  (cond
    (== count 0) []
    (== count 1) [1]
    :else (loop [x (- count 2)
                 fib-nums [1 1]]
            (if (pos? x)
              (recur (dec x) (conj
                              fib-nums
                              (reduce + (take-last 2 fib-nums))))
              fib-nums))))

(comment (fibonacci-sequence 0))
(comment (fibonacci-sequence 1))
(comment (fibonacci-sequence 2))
(comment (fibonacci-sequence 7))

;; 27. Write a function which returns true if the given sequence is a palindrome.
(defn is-palindrome [s]
  (if (<= (count s) 1)
    true
    (let [left (first s)
          right (last s)
          remaining (butlast (rest s))]
      (and (= left right) (recur remaining)))))

(comment (is-palindrome "abc"))
(comment (is-palindrome "aba"))
(comment (is-palindrome [1 1 1 2]))
(comment (is-palindrome [1 1 2 2 1 1]))

;; 28. Write a function which flattens a sequence.
(defn flatten-list [[current & remaining :as items]]
  (when (seq items)
    (if (coll? current)
      (concat (flatten-list current) (flatten-list remaining))
      (cons current (flatten-list remaining)))))

(comment (flatten-list '((1 2) 3 [4 [5 6]])))
(comment (flatten-list ["a" ["b"] "c"]))
(comment (flatten-list '((((:a))))))

;; 29. Write a function which takes a string and returns a new string containing
;; only the capital letters.
(defn filter-capital-letters [[current & remaining :as s]]
  (if (seq s)
    (if (Character/isUpperCase current)
      (string/join [current (filter-capital-letters remaining)])
      (filter-capital-letters remaining))
    ""))

(comment (filter-capital-letters "HeLlO, WoRlD!"))
(comment (filter-capital-letters "nothing"))
(comment (filter-capital-letters "$#A(*&987Zf"))


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


(comment (apply str (remove-consecutive-duplicates "Leeeeeerrroyyy")))
(comment (remove-consecutive-duplicates [1 1 2 3 3 2 2 3]))
(comment (remove-consecutive-duplicates [[1 2] [1 2] [3 4] [1 2]]))


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

(comment (pack-sequence [1 1 2 1 1 1 3 3]))
(comment (pack-sequence [:a :a :b :b :c]))
(comment (pack-sequence [[1 2] [1 2] [3 4]]))

;; 32. Write a function which duplicates each element of a sequence.
(defn duplicate-elements [items]
  (reduce #(conj %1 %2 %2) [] items))

(comment (duplicate-elements [1 2 3]))
(comment (duplicate-elements [:a :a :b :b]))
(comment (duplicate-elements [[1 2] [3 4]]))
(comment (duplicate-elements [44 33]))

;; 33. Write a function which replicates each element of a sequence a variable
;; number of times.
(defn replicate-n-times [items times]
  (reduce #(concat %1 (repeat times %2)) '() items))

(comment (replicate-n-times [1 2 3] 2))
(comment (replicate-n-times [:a :b] 4))
(comment (replicate-n-times [4 5 6] 1))
(comment (replicate-n-times [[1 2] [3 4]] 2))
(comment (replicate-n-times [44 33] 2))

;; 34. Write a function which creates a list of all integers in a given range.
(defn range* [start end]
  (loop [current start
         items []]
    (if (< current end)
      (recur (inc current) (conj items current))
      (seq items))))

(comment (range* 1 4))
(comment (range* -2 2))
(comment (range* 5 8))

;; 35-37 on 4Clojure.

;; 38. Write a function which takes a variable number of parameters and returns
;; the maximum value.
(defn max* [& items]
  (reduce #(if (< %1 %2) %2 %1) items))

(comment (max* 1 8 3 4))
(comment (max* 30 20))
(comment (max* 45 67 11))

;; 39. Write a function which takes two sequences and returns the first item
;; from each, then the second item from each, then the third, etc.
(defn interleave* [items-1 items-2]
  (loop [items-1 items-1
         items-2 items-2
         interleaved (empty items-1)]
    (if (and (seq items-1) (seq items-2))
      (recur
       (rest items-1)
       (rest items-2)
       (conj interleaved (first items-1) (first items-2)))
      interleaved)))

(comment (interleave* [1 2 3] [:a :b :c]))
(comment (interleave* [1 2] [3 4 5 6]))
(comment (interleave* [1 2 3 4] [5]))
(comment (interleave* [30 20] [25 15]))

;; 40. Write a function which separates the items of a sequence by an arbitrary
;;     value.
(defn interpose* [separator items]
  (pop (reduce #(conj %1 %2 separator) [] items)))

(comment (interpose* 0 [1 2 3]))
(comment (apply str (interpose* ", " ["one" "two" "three"])))
(comment (interpose* :z [:a :b :c :d]))

;; 41. Write a function which drops every Nth item from a sequence.
(defn drop-nth [items n]
  (loop [[current & remaining :as items] items
         index 1
         result []]
    (if items
      (recur
       remaining
       (inc index)
       (if (zero? (mod index n))
         result
         (conj result current)))
      result)))

(comment (drop-nth [1 2 3 4 5 6 7 8] 3))
(comment (drop-nth [:a :b :c :d :e :f] 2))
(comment (drop-nth [1 2 3 4 5 6] 4))

;; 42. Write a function which calculates factorials.
(defn factorial [n]
  (reduce #(* %1 (inc %2)) 1 (range n)))

(comment (factorial 1))
(comment (factorial 3))
(comment (factorial 5))
(comment (factorial 8))

;; 43. Write a function which reverses the interleave process into x number of
;;     subsequences.
(defn reverse-interleave [items layer-count]
  (loop [[current & remaining :as items] items
         index 0
         result (vec (repeat layer-count []))]
    (if items
      (recur
       remaining
       (inc index)
       (let [current-seq-index (mod index layer-count)]
         (update result current-seq-index conj current)))
      result)))

(comment (reverse-interleave [1 2 3 4 5 6] 2))
(comment (reverse-interleave (range 9) 3))
(comment (reverse-interleave (range 10) 5))

;; 44. Write a function which can rotate a sequence in either direction.
(defn rotate [n items]
  (let [c (count items)
        n (if (> n 0)
            (mod n c)
            (- c (mod (abs n) c)))]
    (concat (drop n items) (take n items))))

(comment (rotate 2 [1 2 3 4 5]))
(comment (rotate -2 [1 2 3 4 5]))
(comment (rotate 6 [1 2 3 4 5]))
(comment (rotate 1 '(:a :b :c)))
(comment (rotate -4 '(:a :b :c)))

;; 45. The iterate function can be used to produce an infinite lazy sequence.
(comment (take 5 (iterate #(+ 3 %) 1)))

;; 46. Write a higher-order function which flips the order of the arguments of
;;     an input function.
(defn flip-args [f]
  (fn [& args] (apply f (reverse args))))

(comment ((flip-args nth) 2 [1 2 3 4 5]))

;; 47-48 on 4Clojure.

;; 49. Write a function which will split a sequence into two parts.
(defn split-sequence [at-pos items]
  [(vec (take at-pos items)) (vec (drop at-pos items))])

(comment (split-sequence 3 [1 2 3 4 5 6]))
(comment (split-sequence 1 [:a :b :c :d]))
(comment (split-sequence 2 [[1 2] [3 4] [5 6]]))

;; 50. Write a function which takes a sequence consisting of items with
;;     different types and splits them up into a set of homogeneous
;;     sub-sequences. The internal order of each sub-sequence should be
;;     maintained, but the sub-sequences themselves can be returned in any order
;;     (this is why 'set' is used in the test cases).
(defn classify [items]
  (loop [[current & remaining :as items] items
         classified {}]
    (if items
      (recur
       remaining
       (let [t (type current)]
         (if (contains? classified t)
           (update classified t conj current)
           (assoc classified t [current]))))
      (set (vals classified)))))

(comment (classify [1 :a 2 :b 3 :c]))
(comment (classify [:a "foo"  "bar" :b]))
(comment (classify [[1 2] :a [3 4] 5 6 :b]))

;; 51-52 on 4Clojure.

;; 53. Given a vector of integers, find the longest consecutive sub-sequence of
;;     increasing numbers. If two sub-sequences have the same length, use the
;;     one that occurs first. An increasing sub-sequence must have a length of 2
;;     or greater to qualify.
(defn longest-increasing-subseqeuence [[current & remaining :as nums]]
  (if (> 2 (count nums))
    []
    (loop [current-longest [current]
           longest [current]
           [current & remaining] remaining]
      (if (nil? current)
        (if (< 1 (count longest)) longest [])
        (let [new-current-longest (if (< (peek current-longest) current)
                                    (conj current-longest current)
                                    [current])
              new-longest (if (< (count longest) (count new-current-longest))
                            new-current-longest
                            longest)]
          (recur new-current-longest new-longest remaining))))))

(comment (longest-increasing-subseqeuence [1 0 1 2 3 0 4 5]))
(comment (longest-increasing-subseqeuence [5 6 1 3 2 7]))
(comment (longest-increasing-subseqeuence [2 3 3 4 5]))
(comment (longest-increasing-subseqeuence [7 6 5 4]))

;; 54. Write a function which returns a sequence of lists of x items each. Lists
;;     of less than x items should not be returned.
(defn partition* [n items]
  (loop [partitioned []
         remaining items]
    (if (> n (count remaining))
      (seq partitioned)
      (recur (conj partitioned (take n remaining)) (drop n remaining)))))

(comment (partition* 3 (range 9)))
(comment (partition* 2 (range 8)))
(comment (partition* 3 (range 8)))

;; 55. Write a function which returns a map containing the number of occurences
;;     of each distinct item in a sequence.
(defn count-freq [items]
  (reduce
   (fn [freqs current]
     (if (contains? freqs current)
       (update freqs current inc)
       (assoc freqs current 1)))
   {}
   items))

(comment (count-freq [1 1 2 3 2 1 1]))
(comment (count-freq [:b :a :b :a :b]))
(comment (count-freq '([1 2] [1 3] [1 3])))

;; 56. Write a function which removes the duplicates from a sequence. Order of
;;     the items must be maintained.
(defn distinct* [items]
  (loop [item-set #{}
         result []
         [current & remaining] items]
    (cond
      (nil? current) result
      (contains? item-set current) (recur item-set result remaining)
      :else (recur (conj item-set current) (conj result current) remaining))))

(comment (distinct* [1 2 1 3 1 2 4]))
(comment (distinct* [:a :a :b :b :c :c]))
(comment (distinct* '([2 4] [1 2] [1 3] [1 3])))
(comment (distinct* (range 50)))

;; 57 on 4Clojure.

;; 58. Write a function which allows you to create function compositions. The
;;     parameter list should take a variable number of functions, and create a
;;     function applies them from right-to-left.
(defn comp* [& fns]
  (fn [& args]
    (loop [[current & remaining] (reverse fns)
           result args]
      (if (nil? current)
        (first result)
        (recur remaining (list (apply current result)))))))

(comment ((comp* rest reverse) [1 2 3 4]))
(comment ((comp* (partial + 3) second) [1 2 3 4]))

;; 59. Take a set of functions and return a new function that takes a variable
;;     number of arguments and returns a sequence containing the result of
;;     applying each function left-to-right to the argument list.
(defn juxt* [& fns]
  (fn [& args]
    (loop [[current & remaining] fns
           result []]
      (if (nil? current)
        result
        (recur remaining (conj result (apply current args)))))))

(comment ((juxt* + max min) 2 3 5 1 6 4))
(comment ((juxt* #(.toUpperCase %) count) "hello"))
(comment ((juxt* :a :c :b) {:a 2 :b 4 :c 6 :d 8 :e 10}))

;; 60. Write a function which behaves like reduce, but returns each intermediate
;;     value of the reduction. Your function must accept either two or three
;;     arguments, and the return sequence must be lazy.
(defn reductions*
  ([f coll] (reductions* f (first coll) (rest coll)))
  ([f val coll] (lazy-seq
                 (if (empty? coll)
                   [val]
                   (cons val (reductions* f (f val (first coll)) (rest coll)))))))

(comment (take 5 (reductions* + (range))))
(comment (reductions* conj [1] [2 3 4]))
(comment (reductions* * 2 [3 4 5]))

;; 61. Write a function which takes a vector of keys and a vector of values and
;;     constructs a map from them.
(defn zipmap* [keys values]
  (loop [result {}
         [current-key & remaining-keys] keys
         [current-value & remaining-values] values]
    (if (or (nil? current-key) (nil? current-value))
      result
      (recur (conj result {current-key current-value}) remaining-keys remaining-values))))

(comment (zipmap* [:a :b :c] [1 2 3]))
(comment (zipmap* [1 2 3 4] ["one" "two" "three"]))
(comment (zipmap* [:foo :bar] ["foo" "bar" "baz"]))

;; 62. Given a side-effect free function f and an initial value x write a
;;     function which returns an infinite lazy sequence of x, (f x), (f (f x)),
;;     (f (f (f x))), etc.
(defn iterate* [f x]
  (lazy-seq (cons x (iterate* f (apply f [x])))))

(comment (take 5 (iterate* #(* 2 %) 1)))
(comment (take 100 (iterate* inc 0)))
(comment (take 9 (iterate* #(inc (mod % 3)) 1)))

;; 63. Given a function f and a sequence s, write a function which returns a
;;     map. The keys should be the values of f applied to each item in s. The
;;     value at each key should be a vector of corresponding items in the order
;;     they appear in s.
(defn group-by* [f s]
  (reduce
   (fn [acc current]
     (let [v (apply f [current])]
       (if (contains? acc v)
         (update acc v conj current)
         (assoc acc v [current]))))
   {}
   s))

(comment (group-by* #(> % 5) #{1 3 6 8}))
(comment (group-by* #(apply / %) [[1 2] [2 4] [4 6] [3 6]]))
(comment (group-by* count [[1] [1 2] [3] [1 2 3] [2 3]]))

;; 64 on 4Clojure.

;; 65. Clojure has many collection types, which act in subtly different ways.
;;     The core functions typically convert them into a uniform "sequence" type
;;     and work with them that way, but it can be important to understand the
;;     behavioral and performance differences so that you know which kind is
;;     appropriate for your application. Write a function which takes a
;;     collection and returns one of :map, :set, :list, or :vector - describing
;;     the type of collection it was given. You won't be allowed to inspect
;;     their class or use the built-in predicates like list? - the point is to
;;     poke at them and understand their behavior.
;;
;; Special Restrictions:
;; class,type,Class,vector?,sequential?,list?,seq?,map?,set?,instance?,getClass
(defn type-coll [coll]
  (let [c (+ 3 (count coll))
        coll (conj coll [:a :b] [:a :b] [:c :d])
        nc (count coll)]
    (cond
      (= :b (get coll :a)) :map
      (not= c nc) :set
      (= [:c :d] (first coll)) :list
      (= [:c :d] (last coll)) :vector)))

(comment (type-coll {:a 1, :b 2}))
(comment (type-coll (range (rand-int 20))))
(comment (type-coll [1 2 3 4 5 6]))
(comment (type-coll #{10 (rand-int 5)}))
(comment (map type-coll [{} #{} [] ()]))

;; 66. Given two integers, write a function which returns the greatest common
;;     divisor.
;;
;; https://en.wikipedia.org/wiki/Euclidean_algorithm
(defn gcd [x y]
  (loop [a (max x y)
         b (min x y)]
    (if (zero? b)
      a
      (recur b (mod a b)))))

(comment (gcd 2 4))
(comment (gcd 10 5))
(comment (gcd 5 7))
(comment (gcd 1023 858))

;; 67. Write a function which returns the first x number of prime numbers.
(defn prime-numbers [x]
  (letfn [(prime? [n]
            (cond
              (<= n 1) false
              (<= n 3) true
              :else (loop [[i & remaining] (range 2 (inc (Math/sqrt n)))]
                      (cond
                        (nil? i) true
                        (zero? (rem n i)) false
                        :else (recur remaining)))))]
    (take x (filter prime? (range)))))

(comment (prime-numbers 2))
(comment (prime-numbers 5))
(comment (last (prime-numbers 100)))

;; 68 on 4Clojure

;; 69. Write a function which takes a function f and a variable number of maps.
;;     Your function should return a map that consists of the rest of the maps
;;     conj-ed onto the first. If a key occurs in more than one map, the
;;     mapping(s) from the latter (left-to-right) should be combined with the
;;     mapping in the result by calling (f val-in-result val-in-latter)
(defn merge-with* [f first-map & maps]
  (reduce
   (fn [acc current-map]
     (reduce
      (fn [result [key val]]
        (if (contains? result key)
          (update result key f val)
          (assoc result key val)))
      acc
      current-map))
   first-map
   maps))

(comment (merge-with* * {:a 2, :b 3, :c 4} {:a 2} {:b 2} {:c 5}))
(comment (merge-with* - {1 10, 2 20} {1 3, 2 10, 3 15}))
(comment (merge-with* concat {:a [3], :b [6]} {:a [4 5], :c [8 9]} {:b [7]}))

;; 70. Write a function which splits a sentence up into a sorted list of words.
;;     Capitalization should not affect sort order and punctuation should be
;;     ignored.
(defn split-sort-words [s]
  (sort
   #(compare (string/lower-case %1) (string/lower-case %2))
   (re-seq #"\w+" s)))

(comment (split-sort-words "Have a nice day."))
(comment (split-sort-words "Clojure is a fun language!"))
(comment (split-sort-words "Fools fall for foolish follies."))

;; 71-72 on 4Clojure.

;; 73. A tic-tac-toe board is represented by a two dimensional vector. X is
;;     represented by :x, O is represented by :o, and empty is represented by
;;     :e. A player wins by placing three Xs or three Os in a horizontal,
;;     vertical, or diagonal row. Write a function which analyzes a tic-tac-toe
;;     board and returns :x if X has won, :o if O has won, and nil if neither
;;     player has won.
(defn check-vector [board vec-to-check]
  (let [vals  (map (fn [cell] (get-in board cell)) vec-to-check)]
    (when (and (not= :e (first vals)) (apply = vals)) (first vals))))

(defn winner? [board]
  (let [winning-vectors (concat
                         (partition 3 (for [x (range 3) y (range 3)] [x y])) ; rows
                         (partition 3 (for [x (range 3) y (range 3)] [y x])) ; columns
                         (list (list [0 0] [1 1] [2 2]) (list [2 0] [1 1] [0 2])))] ; diagonals
    (reduce
     (fn [winner curr] (if  (nil? winner) (check-vector board curr) winner))
     nil
     winning-vectors)
    (first (keep #(check-vector board %) winning-vectors))))

(comment (winner? [[:e :e :e]
                   [:e :e :e]
                   [:e :e :e]]))

(comment (winner? [[:x :e :o]
                   [:x :e :e]
                   [:x :e :o]]))

(comment (winner? [[:e :x :e]
                   [:o :o :o]
                   [:x :e :x]]))

(comment (winner? [[:x :e :o]
                   [:x :x :e]
                   [:o :x :o]]))

(comment (winner? [[:x :e :e]
                   [:o :x :e]
                   [:o :e :x]]))

(comment (winner? [[:x :e :o]
                   [:x :o :e]
                   [:o :e :x]]))

(comment (winner? [[:x :o :x]
                   [:x :o :x]
                   [:o :x :o]]))

;; 74. Given a string of comma separated integers, write a function which
;;     returns a new comma separated string that only contains the numbers which
;;     are perfect squares.
(defn perfect-squares [s]
  (string/join
   ","
   (reduce
    (fn [perfect-squares num]
      (let [root (Math/sqrt num)]
        (if (zero? (mod root (long root)))
          (conj perfect-squares num)
          perfect-squares)))
    []
    (map parse-long (re-seq #"\d+" s)))))


(comment (perfect-squares "4,5,6,7,8,9"))
(comment (perfect-squares "15,16,25,36,37"))

;; 75. Two numbers are coprime if their greatest common divisor equals 1.
;;     Euler's totient function f(x) is defined as the number of positive
;;     integers less than x which are coprime to x. The special case f(1) equals
;;     1. Write a function which calculates Euler's totient function.
(defn eulers-totient [x]
  (if (= x 1)
    1
    (reduce
     (fn [count current]
       (if (empty? (filter #(= 0 (rem x %1) (rem current %1)) (range 2 (inc current))))
         (inc count)
         count))
     0
     (range 1 x))))

(comment (eulers-totient 1))
(comment (eulers-totient 10))
(comment (eulers-totient 40))
(comment (eulers-totient 99))

;; 76 on 4Clojure.

;; 77. Write a function which finds all the anagrams in a vector of words. A
;;     word x is an anagram of word y if all the letters in x can be rearranged
;;     in a different order to form y. Your function should return a set of
;;     sets, where each sub-set is a group of words which are anagrams of each
;;     other. Each sub-set should have at least two words. Words without any
;;     anagrams should not be included in the result.
(defn group-anagrams [words]
  (filter
   #(< 1 (count %1))
   (vals
    (reduce
     (fn [anagrams current]
       (let [key (string/join (sort current))]
         (if (contains? anagrams key)
           (update anagrams key conj current)
           (assoc anagrams key #{current}))))
     {}
     words))))

(comment (group-anagrams ["meat" "mat" "team" "mate" "eat"]))
(comment (group-anagrams ["veer" "lake" "item" "kale" "mite" "ever"]))

;; 78. Reimplement the function described in "Intro to Trampoline".
(defn trampoline* [f & args]
  (loop [result (apply f args)]
    (if (fn? result)
      (recur (result))
      result)))

(comment (letfn [(triple [x] #(sub-two (* 3 x)))
                 (sub-two [x] #(stop? (- x 2)))
                 (stop? [x] (if (> x 50) x #(triple x)))]
           (trampoline* triple 2)))
(comment (letfn [(my-even? [x] (if (zero? x) true #(my-odd? (dec x))))
                 (my-odd? [x] (if (zero? x) false #(my-even? (dec x))))]
           (map (partial trampoline* my-even?) (range 6))))


;; 79. Write a function which calculates the sum of the minimal path through a
;;     triangle. The triangle is represented as a vector of vectors. The path
;;     should start at the top of the triangle and move to an adjacent number on
;;     the next row until the bottom of the triangle is reached.
(defn minimal-path-in-triangle
  ([t] (minimal-path-in-triangle t [0 0]))
  ([t [start-x start-y]]
   (if (< start-y (count t))
     (+
      (get-in t [start-y start-x])
      (min
       (minimal-path-in-triangle t [start-x (inc start-y)])
       (minimal-path-in-triangle t [(inc start-x) (inc start-y)])))
     0)))

(comment (minimal-path-in-triangle [[1]
                                    [2 4]
                                    [5 1 4]
                                    [2 3 4 5]]))

(comment (minimal-path-in-triangle [[3]
                                    [2 4]
                                    [1 9 3]
                                    [9 9 2 4]
                                    [4 6 6 7 8]
                                    [5 7 3 5 1 4]]))

;; 80. A number is "perfect" if the sum of its divisors equal the number itself.
;;     6 is a perfect number because 1+2+3=6. Write a function which returns
;;     true for perfect numbers and false otherwise.
(defn perfect-divisor? [n]
  (=
   n
   (reduce
    (fn [sum current]
      (if (zero? (rem n current))
        (+ sum current)
        sum))
    1
    (range 2 n))))

(comment (perfect-divisor? 6))
(comment (perfect-divisor? 7))
(comment (perfect-divisor? 496))
(comment (perfect-divisor? 500))
(comment (perfect-divisor? 8128))

;; 81. Write a function which returns the intersection of two sets. The
;;     intersection is the sub-set of items that each set has in common.
;;
;; Special Restrictions : intersection
(defn intersection* [& sets]
  (reduce #(set (filter %1 %2)) (first sets) (rest sets)))

(comment (intersection* #{0 1 2 3} #{2 3 4 5}))
(comment (intersection* #{0 1 2} #{3 4 5}))
(comment (intersection* #{:a :b :c :d} #{:c :e :a :f :d}))

;; 82. A word chain consists of a set of words ordered so that each word differs
;;     by only one letter from the words directly before and after it. The one
;;     letter difference can be either an insertion, a deletion, or a
;;     substitution. Here is an example word chain: cat -> cot -> coat -> oat ->
;;     hat -> hot -> hog -> dog Write a function which takes a sequence of
;;     words, and returns true if they can be arranged into one continous word
;;     chain, and false if they cannot.
(defn- neighbour? [w1 w2]
  (if
   (or (empty? w1) (empty? w2))
    true
    (= 1 (m/levenshtein w1 w2))))

(defn word-chain?
  ([words] (word-chain? words []))
  ([words chain]
   (if (empty? words)
     true
     (some
      (fn [[current & remaining]]
        (when (neighbour? (peek chain) current)
          (word-chain? (into #{} remaining) (conj chain current))))
      (map #(rotate % words) (range (count words)))))))

(comment (word-chain? #{"hat" "coat" "dog" "cat" "oat" "cot" "hot" "hog"}))
(comment (word-chain? #{"cot" "hot" "bat" "fat"}))
(comment (word-chain? #{"to" "top" "stop" "tops" "toss"}))
(comment (word-chain? #{"spout" "do" "pot" "pout" "spot" "dot"}))
(comment (word-chain? #{"share" "hares" "shares" "hare" "are"}))
(comment (word-chain? #{"share" "hares" "hare" "are"}))

;; 83. Write a function which takes a variable number of booleans. Your function
;;     should return true if some of the parameters are true, but not all of the
;;     parameters are true. Otherwise your function should return false.
(defn some* [& args]
  (let [total (count args)
        true-count (count (filter true? args))]
    (< 0 true-count total)))

(comment (some* false false))
(comment (some* true false))
(comment (some* true))
(comment (some* false true false))
(comment (some* true true true))
(comment (some* true true true false))

;; 84. Write a function which generates the transitive closure of a binary
;;     relation. The relation will be represented as a set of 2 item vectors.
;; e.g. [a -> b] [b -> c] [c -> d] => [a -> d]
(defn transitive-closures [relations]
  (let [derived-relations (remove
                           nil?
                           (for [[aa ab] relations
                                 [ba bb] relations] (when (= ab ba) [aa bb])))
        all-relations (into relations derived-relations)]
    (if (= all-relations relations)
      relations
      (recur all-relations))))

(comment (transitive-closures #{[8 4] [9 3] [4 2] [27 9]}))
(comment (transitive-closures  #{["cat" "man"] ["man" "snake"] ["spider" "cat"]}))
(comment (transitive-closures #{["father" "son"] ["uncle" "cousin"] ["son" "grandson"]}))

;; 85. Write a function which generates the power set of a given set. The power
;;     set of a set x is the set of all subsets of x, including the empty set
;;     and x itself.
(defn power-set [xs]
  (->> xs
       (reduce (fn [res x]
                 (->> res
                      (map #(apply hash-set x %))
                      (cons #{x})
                      (into res)))
               #{})
       (into #{#{}})))

(comment (power-set #{1 :a}))
(comment (power-set #{}))
(comment (power-set #{1 2 3}))
