(ns clojure-sandbox.4clojure)

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
