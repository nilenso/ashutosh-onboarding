(ns clojure-sandbox.chapter-4)

;; floating-point arithmetic may produce different results based on the order of
;; operations.
(def a  1.0e50)
(def b -1.0e50)
(def c 17.0e00)

(+ (+ a b) c) ;=> 17.0
(+ a (+ b c)) ;=> 0.0

;; using rational numbers to perform calculation will mitigate the issue.
(def a (rationalize 1.0e50))
(def b (rationalize -1.0e50))
(def c (rationalize 17.0e00))

(+ (+ a b) c) ;=> 17N
(+ a (+ b c)) ;=> 17N

;; using keywords perform lookup in a map.
(:a {:a "a" :b "b"})

;; namespace qualified keyword. should expand to `:clojure-sandbox.chapter-4/abc`
'(::abc)

;; unlike symbols, same keyword are always identical. Equality for symbols
;; depends on their name.
(= 'a 'a) ;=> true
(identical? 'a 'a) ;=> false
(= :a :a) ;=> true
(identical? :a :a) ;=> true

;; metadata?
(let [x (with-meta 'a {:b true})
      y (with-meta 'a {:b false})]
  (prn (= x y))
  (prn (identical? x y))
  (prn (meta x))
  (prn (meta y)))

(re-seq #"\d" "123abc456")
