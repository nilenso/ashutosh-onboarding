(ns clojure-sandbox.chapter-15
  (:require [clojure.core.reducers :as r]))

(defn asum-sq ^Long [^ints xs]
  (let [^ints sq-xs (amap xs i _ (* (aget xs i) (aget xs i)))]
    (areduce
     sq-xs
     i
     ret
     0
     (+ ret (aget sq-xs i)))))

(.intValue (asum-sq (int-array [1 2 3 4 5])))

(time (dotimes [_ 100000] (asum-sq (int-array [1 2 3 4 5]))))

(defn zencat1 [x y]
  (loop [src y, ret x]
    (if (seq src)
      (recur (next src) (conj ret (first src)))
      ret)))

(time (dotimes [_ 1000000] (zencat1 [1 2 3] [4 5 6])))

(defn zencat2 [x y]
  (loop [src y, ret (transient x)]
    (if src
      (recur (next src) (conj! ret (first src)))
      (persistent! ret))))

;; takes longer than zencat1 because we're concatenating very small vectors a
;; large number of times.
(time (dotimes [_ 1000000] (zencat2 [1 2 3] [4 5 6])))

;; outperforms zencat1 if we use it on large vectors.
(let [bv (vec (range 1e6))]
  (time (zencat1 bv bv))
  (time (zencat2 bv bv))
  nil)

;; chunked sequences realise chunks of sequences instead of one item at a time.
(def gimme #(do (print \.) %))
(take 1 (map gimme (range 32)))

;; but, it can be wrapped in another lazy-seq to realise one item at time.
(defn seq1 [s]
  (lazy-seq
   (when-let [[x] (seq s)]
     (cons x (seq1 (rest s))))))

(take 1 (map gimme (seq1 (range 32))))

(defn factorial-a [original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(factorial-a 20)
(time (dotimes [_ 1e5] (factorial-a 20)))

(defn factorial-b [original-x]
  (loop [x (long original-x), acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(time (dotimes [_ 1e5] (factorial-b 20)))

(defn factorial-c [^long original-x]
  (loop [x original-x, acc 1]
    (if (>= 1 x)
      acc
      (recur (dec x) (* x acc)))))

(time (dotimes [_ 1e5] (factorial-b 20)))

(defn factorial-e [^double original-x]
  (loop [x original-x, acc 1.0]
    (if (>= 1.0 x)
      acc
      (recur (dec x) (* x acc)))))

(factorial-e 30.0)
(factorial-e 171.0)
(time (dotimes [_ 1e5] (factorial-e 20.0)))

(defprotocol CacheProtocol
  (lookup  [cache e])
  (has?    [cache e])
  (hit     [cache e])
  (miss    [cache e ret]))

(deftype BasicCache [cache]
  CacheProtocol
  (lookup [_ item]
    (get cache item))
  (has? [_ item]
    (contains? cache item))
  (hit [this item] this)
  (miss [_ item result]
    (BasicCache. (assoc cache item result))))

(def cache (BasicCache. {}))

(lookup (miss cache '(servo) :robot) '(servo))

(defn through [cache f item]
  (if (has? cache item)
    (hit cache item)
    (miss cache item (delay (apply f item)))))

(deftype PluggableMemoization [f cache]
  CacheProtocol
  (has? [_ item] (has? cache item))
  (hit  [this item] this)
  (miss [_ item result]
    (PluggableMemoization. f (miss cache item result)))
  (lookup [_ item]
    (lookup cache item)))

(defn memoization-impl [cache-impl]
  (let [cache (atom cache-impl)]
    (with-meta
      (fn [& args]
        (let [cs (swap! cache through (.f cache-impl) args)]
          @(lookup cs args)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1500) x))
(def sometimes-slowly (memoization-impl
                       (PluggableMemoization.
                        slowly
                        (BasicCache. {}))))

(time [(sometimes-slowly 108) (sometimes-slowly 108)])
(time [(sometimes-slowly 108) (sometimes-slowly 108)])

(defn empty-range? [start end step]
  (or (and (pos? step) (>= start end))
      (and (neg? step) (<= start end))))

(defn lazy-range [start end step]
  (lazy-seq
   (if (empty-range? start end step)
     nil
     (cons start (lazy-range (+ start step) end step)))))


(lazy-range 1 10 2)
(reduce + 0 (lazy-range 1 100 2))


;; returns `fn [reducing-fn init]`
(defn reducible-range [start end step]
  (fn [reducing-fn init]
    (loop [result init
           i start]
      (if (empty-range? i end step)
        result
        (recur (reducing-fn result i) (+ i step))))))

((reducible-range 1 100 2) + 0)

(defn half [n]
  (/ n 2))

(defn sum-half [result input]
  (+ result (half input)))

(reduce sum-half 0 (lazy-range 1 100 2))
((reducible-range 1 100 2) sum-half 0)

(defn half-transformer [fn1]
  (fn fn1-half [result input]
    (fn1 result (half input))))

((reducible-range 0 10 2) (half-transformer +) 0)
((reducible-range 0 10 2) (half-transformer conj) [])

(defn mapping [map-fn]
  (fn map-transformer [f1]
    (fn [result input]
      (f1 result (map-fn input)))))

((reducible-range 0 10 2) ((mapping half) +) 0)

(defn mapcatting [map-fn]
  (fn [f1]
    (fn [result input]
      (let [reducible (map-fn input)]
        (reducible f1 result)))))

(defn and-plus-ten [x]
  (reducible-range x (+ 11 x) 10))

((and-plus-ten 5) conj [])

((reducible-range 0 10 2) ((mapcatting and-plus-ten) conj) [])

(defn filtering [filter-pred]
  (fn [f1]
    (fn [result input]
      (if (filter-pred input)
        (f1 result input)
        result))))

(defn r-map [mapping-fn reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((mapping mapping-fn) reducing-fn) init)))

(defn r-filter [filter-pred reducible]
  (fn new-reducible [reducing-fn init]
    (reducible ((filtering filter-pred) reducing-fn) init)))

((reducible-range 0 10 2) ((filtering #(not= % 2)) +) 0)

((r-filter #(not= % 2) (r-map half (reducible-range 0 10 2))) conj [])

(require '[criterium.core :as crit])
(crit/bench
 (reduce + 0 (filter even? (map half (lazy-range 0
                                                 (* 10 1000 1000) 2)))))

(crit/bench
 (reduce + 0 (filter even? (map half (range 0 (* 10 1000 1000) 2)))))

(crit/bench
 ((r-filter even? (r-map half
                         (reducible-range 0 (* 10 1000 1000) 2))) + 0))

(require '[clojure.core.reducers :as r])

(defn core-r-map [mapping-fn core-reducible]
  (r/reducer core-reducible (mapping mapping-fn)))

(defn core-r-filter [filter-pred core-reducible]
  (r/reducer core-reducible (filtering filter-pred)))

(reduce conj []
        (core-r-filter #(not= % 2)
                       (core-r-map half [0 2 4 6 8])))

(r/fold + [1 2 3 4 5])
