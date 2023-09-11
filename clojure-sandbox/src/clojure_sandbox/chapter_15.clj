(ns clojure-sandbox.chapter-15)

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
