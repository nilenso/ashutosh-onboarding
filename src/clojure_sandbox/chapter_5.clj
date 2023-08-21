(ns clojure-sandbox.chapter-5
  (:require [clojure.set :as set]))

(let [array (into-array (range 10))] ; build a Java array
  (prn (seq array)) ; pretty print the Array Object
  (aset array 0 10) ; set item at index 0 to 10
  (prn (seq array)))

;; Clojure DSs produce a new copy of the modified object
(let [v0 [1 2 3 4 5]
      v1 (replace {1 0} v0)]
  (prn v0)
  (prn v1))

;; vector and list are equal, the other two are not because vectors and lists are
;; sequential collections and a set is not.
(= [1 2 3] '(1 2 3))
(= [1 2 3] #{1 2 3})
(= '(1 2 3) #{1 2 3})

(seq (hash-map :a 1, :b 2)) ; returns a node sequence

(into (vec (range 10)) (range 10 20))
(into (vector-of :char) [100 101 102])
([1 2 3 4] 0) ; just like a map, but throws on nil vector and index out of bounds.
(get [] 10 :not-found) ; return a default value for nil vector and out of bounds.

(seq [1 2 3])
(rseq [1 2 3])
(assoc [1 2 3 4] 0 :one)
(replace {1 2} [1 2 3])

(-> [[1 2 3]
     [4 5 6]
     [7 8 9]]
    (update-in [0 0] inc)
    (update-in [2 0] * 10)
    (assoc-in [1 0] 'x))

(peek [1 2])
(pop [1 2])
(conj [1 2] 3)

(doseq [[dimension amount] {:width 10, :height 20, :depth 15}]
  (println (str (name dimension) ":") amount "inches"))


(conj clojure.lang.PersistentQueue/EMPTY 1 2 3)
(peek (conj clojure.lang.PersistentQueue/EMPTY 1 2 3))
(pop clojure.lang.PersistentQueue/EMPTY)
(pop (conj clojure.lang.PersistentQueue/EMPTY 1 2 3))
(rest (conj clojure.lang.PersistentQueue/EMPTY 1 2 3)) ; no more a queue

(#{1 2 3 4} 5)
(#{1 2 3 4} 3)

(into #{[]} ['()]) ; list doesn't get added because of equality.

; Using a set as the predicate supplied to some allows you to check whether any
; of the truthy values in the set are contained within the given sequence. This
; is a frequently used Clojure idiom for searching for containment within a
; sequence.
(some #{1 :b} [:a 1 :b 2])
(some #{:b} [:a 1 :b 2])

(sorted-set 3 2 1)
(sorted-set :c :b :a)

;; doesn't work because elements aren't comparable.
; (sorted-set :b 2 :c :a 3 1)

(set/intersection #{:a :b :c}
                  #{:d :e :a}
                  #{:g :a :i})

(set/union #{:a :b :c}
           #{:d :e :a}
           #{:g :a :i})

;; relative complement and not exactly a difference.
(set/difference #{:a :b :c} #{:d :e :a})

(zipmap [:a :b :c] [1 2 3])
(sorted-map :c 1 :b 3 :a 2)

; 1 and 1.0 imply the same key in a sorted map.
(assoc {1 :int} 1.0 :float)
(assoc (sorted-map 1 :int) 1.0 :float)


;; use array maps for insertion ordering.
(seq (hash-map :a 1, :b 2, :c 3))
(seq (array-map :a 1, :b 2, :c 3))

(defn index [coll]
  (cond
    (map? coll) (seq coll)
    (set? coll) (map vector coll coll)
    :else (map vector (iterate inc 0) coll)))

(defn pos [e coll]
  (for [[i v] (index coll) :when (= e v)] i))

(pos :a [:a 1 :b 2 :c 3 :d 4])
(pos nil [:a 1 :b 2 :c 3 :d 4])
(pos 0 [:a 1 :b 2 :c 3 :d 4])
