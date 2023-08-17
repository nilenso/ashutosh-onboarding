(ns clojure-sandbox.chapter-2)

;; function with multiple arities.
(defn add
  ([x y] (+ x y))
  ([x y z] (+ x y z))
  ([x y z & rest] (+ x y z (apply + rest))))

(add 1 2)
(add 1 2 3)
(add 1 2 3 4)
(add 1 2 3 4 5)


;; a block will only produce the value of the last expression.(
(do
  (+ 1 2)
  (+ 2 3)
  (+ 3 4))


;; locals
(def x 10)
(let [x 5] (* x x))
(* x x)


;; tail recursion
(defn print-range [x y]
  (when (<= x y)
    (println x)
    (recur (inc x) y)))

(print-range 1 10)

(defn sum-range [init-x y]
  (loop [sum 0, x init-x]
    (if (<= x y)
      (recur (+ sum x) (inc x))
      sum)))

(sum-range 1 4)
