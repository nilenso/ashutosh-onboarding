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


;; a block will only produce the value of the last expression.
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
  (when (<= x y) ; use `when` when the else block is not needed.
    (println x)
    (recur (inc x) y)))

(print-range 1 10)

(defn sum-range [init-x y]
  (loop [sum 0, x init-x]
    (if (<= x y)
      (recur (+ sum x) (inc x))
      sum)))

(sum-range 1 4)

;; quote simply prevents evaluation
'(+ 2 3 4)

;; syntax-quote also qualifies unqualified symbols while preventing evaluation.
`(+ 2 3 4)

;; unquote `~` is used to denote any Clojure expression as requiring evaluation.
`(+ 10 ~(* 3 2))

;; but it doesn't work with a regular quote.
'(+ 10 ~(* 3 2))

;; like unquote, except the result is spliced.
`(+ 10 ~@[1 2 3])

;; using Java static members
(Math/sqrt 2)

;; constructing Java objects
(new java.awt.Point 1 1)

;; or
(java.awt.Point. 1 1)

;; referencing public fields
(.-x (java.awt.Point. 1 1))

;; invoking public methods
(.concat "abc" "def")

;; setting public fields
(let [origin (java.awt.Point. 0 0)]
  (set! (.-x origin) 15)
  (str origin))

;; .. macro
(.. (StringBuilder.) (append "abc") (append "def") (append "ghi") (toString))

;; or using thread-first macro `->`.
(-> (StringBuilder.)
    (.append "abc")
    (.append "def")
    (.append "ghi")
    (.toString))

;; or using `doto` macro, but instead of producing the value of the last expression,
;; it will produce the built object, i.e. a `StringBuilder` in this case.
(doto (StringBuilder.)
  (.append "abc")
  (.append "def")
  (.append "ghi")
  (.toString))


;; try, throw, catch and finally.
(try
  (throw (Exception. "test-error"))
  (catch Exception e (.getMessage e))
  (finally (println "test-finally")))
