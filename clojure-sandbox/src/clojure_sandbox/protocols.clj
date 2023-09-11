(ns clojure-sandbox.protocols)

;; example from The Joy of Clojure: Chapter 1.

(defprotocol Concatenatable
  (catn [this other]))

(extend-type java.util.List
  Concatenatable
  (catn [this other]
    (concat this other)))

(extend-type String
  Concatenatable
  (catn [this other]
    (.concat this other)))

(catn "abc" "def")
(catn (range 5) (range 5 10))
