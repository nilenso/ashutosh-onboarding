(ns clojure-sandbox.chapter-8
  (:require [clojure.walk :as walk]))

(eval (list (symbol "-") 3 2 1))

(defn contextual-eval [ctx expr]
  (eval
   `(let [~@(mapcat (fn [[k v]] [k `'~v]) ctx)]
      ~expr)))

(contextual-eval '{a 1, b 2} '(+ a b))

(defmacro do-until [& clauses]
  (when clauses
    (list 'clojure.core/when (first clauses)
          (if (next clauses)
            (second clauses)
            (throw (IllegalArgumentException.
                    "do-until requires an even number of forms")))
          (cons 'do-until (nnext clauses)))))

(macroexpand-1 '(do-until true (prn 1) false (prn 2)))
(walk/macroexpand-all '(do-until true (prn 1) false (prn 2)))

(defmacro unless [condition & body]
  `(if ~condition
     nil
     (do ~@body)))

(macroexpand-1 '(unless (= 1 1) (prn false)))
(unless false true)

(defmacro def-watched [name & value]
  `(do
     (def ~name ~@value)
     (add-watch (var ~name)
                :re-bind
                (fn [~'key ~'r old# new#]
                  (println old# "->" new#)))))

(walk/macroexpand-all '(def-watched x 2))
(def-watched x (* 12 12))
(def x 0)


(defmacro domain [name & body]
  `{:tag :domain
    :attrs {:name (str '~name)}
    :content [~@body]})

(declare handle-things)
(defmacro grouping [name & body]
  `{:tag :grouping,
    :attrs {:name (str '~name)},
    :content [~@(handle-things body)]})

(declare grok-attrs grok-props)
(defn handle-things [things]
  (for [t things]
    {:tag :thing,
     :attrs (grok-attrs (take-while (comp not vector?) t))
     :content (if-let [c (grok-props (drop-while (comp not vector?) t))]
                [c] [])}))

(defn grok-attrs [attrs]
  (into {:name (str (first attrs))}
        (for [a (rest attrs)]
          (cond
            (list? a) [:isa (str (second a))]
            (string? a) [:comment a]))))

(defn grok-props [props]
  (when props
    {:tag :properties, :attrs nil,
     :content (apply vector (for [p props]
                              {:tag :property,
                               :attrs {:name (str (first p))},
                               :content nil}))}))

(def d (domain man-vs-monster
               (grouping people
                         (Human "A stock human")
                         (Man (isa Human)
                              "A man baby"
                              [name]
                              [has-beard?]))
               (grouping monsters
                         (Chupacabra
                          "A fierce, yet elusive creature"
                          [eats-goats?]))))

(use '[clojure.xml :as xml])
(xml/emit d)

(defmacro resolution [] `x)
(macroexpand '(resolution))

;; x resolves to 9 despite its local binding because of the quote.
(def x 9)
(let [x 109] (resolution))

(defmacro resolution [] 'x)
(macroexpand '(resolution))

;; Voila! x resolves to 109.
(def x 9)
(let [x 109] (resolution))

;; Anaphora - a previously identified subject or object
(defmacro awhen [expr & body]
  `(let [~'it ~expr]
     (if ~'it (do ~@body))))

(awhen [1 2 3] it)


(import [java.io BufferedReader InputStreamReader]
        [java.net URL])
(defn joc-www []
  (-> "https://httpbin.org/user-agent" URL.
      .openStream
      InputStreamReader.
      BufferedReader.))
(comment (let [stream (joc-www)]
           (with-open [page stream]
             (println (.readLine page))
             (print "The stream will now close... "))
           (println "but let's read from it anyway.")
           (.readLine stream)))

;; let start with something simple
(defmacro add [& args]
  `(+ ~@args))

(add 1 2 3 4)
(comment (add [1])) ; error

;; lets flatten the args list
(defmacro add* [& args]
  `(+ ~@(flatten args)))

(add* [1 2] 3 4)
;; flatten is evaluated when macro expands.
(macroexpand '(add* [1 2] 3 4))

(let [a 1 b 2]
  (add* [a b] 3 4))

(let [a 1 b 2]
  (macroexpand '(add [a b] 3 4)))

;; let's try to implement the when macro
(defmacro when* [condition & body]
  `(if ~condition
     (do ~@body)))

(when* true :truthy)
(when* false :truthy)

(defmacro infix [operand1 operator operand2]
  (list operator operand1 operand2))

(macroexpand (let [a 4 b 6] '(infix a + b)))

(walk/macroexpand-all '(infix (infix 1 + 3) - 2))
(infix (infix 1 + 3) - 2)


;; as macro
(defmacro and*
  ([] true)
  ([expr] expr)
  ([expr & exprs]
   `(let [and# ~expr]
      (if and# (and* ~@exprs) and#))))

(walk/macroexpand-all '(and* true (&& false true true)))
(and* true 1 2 nil)

;; or macro
(defmacro or*
  ([] false)
  ([expr] expr)
  ([expr & exprs]
   `(let [or# ~expr]
      (if or# or# (or* ~@exprs)))))

(walk/macroexpand-all '(or* true (&& false true true)))
(or* false 1 2 nil)

(defmacro criticise-code [bad-code good-code]
  (letfn [(generate-criticism [msg code]
            `(prn ~msg ~code))]
    `(do
       ~(generate-criticism "peasant code" bad-code)
       ~(generate-criticism "gold standard" good-code))))

(walk/macroexpand-all '(criticise-code 1 2))
(criticise-code 'a 'b)



(defmacro contract [name & bodies]
  (letfn [(collect-body [body]
            (let [args (first body)
                  conditions (rest body)]
              (list
               (into ['f] args)
               (apply merge (for [condition conditions]
                              (cond
                                (= :require (first condition)) (assoc {} :pre (second condition))
                                (= :ensure (first condition)) (assoc {} :post (second condition))
                                :else (throw (UnsupportedOperationException. (str "unknown condition" (first condition)))))))
               (cons 'f args))))
          (collect-bodies [bodies]
            (for [body (partition 3 bodies)]
              (collect-body body)))]
    `(fn ~name ~@(collect-bodies bodies))))

;; The following should give:
;; (fn doubler
;;   ([f x]
;;    {:post [(= (* 2 x) %)]
;;     :pre [(pos? x)]}
;;    (f x)))
(macroexpand-1 '(contract doubler [x] (:require (pos? x)) (:ensure (= (* x 1) %))))

(def
  doubler-contract
  (contract doubler [x] (:require (pos? x)) (:ensure (= (* 1 x) %))))

(def times2 (partial doubler-contract #(* 2 %)))
(times2 2)


(defmacro test* [& args]
  `{:args '~args :form '~&form :env ~&env})

(macroexpand-1 (let [a 1 b 2] `(test* ~a ~b 3 4)))

(comment (let [a 1 b 2] (test* 1 2 3 4)))

(defmacro show-env [] (println &env))
(let [band "zeppelin" city "london"] (show-env))

;; BraveClojure problems

;; When the data is valid, the println and render forms should be evaluated, and
;; when-valid should return nil if the data is invalid.
(defn valid?
  "Returns false if data doesn't pass any validations. Returns logical true otherwise."
  [data validations]
  (let [validation-fns (map
                        (fn [[key msg-fn-pairs]] [key (take-nth 2 (rest msg-fn-pairs))])
                        validations)] ; discard error messages from validations.
    (every?
     (fn [[key fns]]
       (let [v (get data key)]
         (every? #(% v) fns)))
     validation-fns)))

(defn explain-errors
  "Returns a map of keys in data to error messages for failed validations."
  [data validations]
  (reduce (fn [errors [key msg-fn-pairs]]
            (let [v (get data key)
                  msgs (remove nil?
                               (map (fn [[msg validation-fn]]
                                      (when-not (validation-fn v) msg))
                                    (partition 2 msg-fn-pairs)))]
              (if (empty? msgs)
                errors
                (conj errors {key msgs}))))
          {}
          validations))

(defmacro when-valid [data validations & body]
  `(let [errors# (explain-errors ~data ~validations)]
     (if (empty? errors#)
       (do ~@body)
       errors#)))

(def order-details
  {:name ""
   :email ""})

(def order-details-validations
  {:name
   ["Please enter a name" not-empty]

   :email
   ["Please enter an email address" not-empty
    "Your email address doesn't look like an email address"
    #(or (empty? %) (re-seq #"@" %))]})

(when-valid order-details order-details-validations
            (println "It's a success!")
            :success)
