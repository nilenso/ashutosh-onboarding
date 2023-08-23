(ns clojure-sandbox.chapter-6)

;; structural sharing: immutable objects can shared between multiple objects without a worry.
(let [base (list :a :b)
      l1 (conj base :c)
      l2 (conj base :d)]
  (identical? (next l1) (next l2)))

;; BST with structural sharing.
(defn xconj [t v]
  (let [root-val (:val t)
        left-subtree (:L t)
        right-subtree (:R t)]
    (cond
      (empty? t) {:val v, :L nil, :R nil}
      (< v root-val) {:val root-val
                      :L (xconj left-subtree v)
                      :R right-subtree}
      :else {:val root-val
             :L left-subtree
             :R (xconj right-subtree v)})))

(defn xseq [t]
  (when t
    (concat (xseq (:L t)) [(:val t)] (xseq (:R t)))))


(-> {}
    (xconj 6)
    (xconj 5)
    (xconj 4)
    (xconj 3)
    (xconj 2)
    (xconj 1)
    (xseq))

(defn delay-demo [cheap expensive]
  (if cheap
    cheap
    (force expensive)))

(delay-demo :cheap (delay (do (Thread/sleep 5000) :expensive)))
(delay-demo nil (delay (do (Thread/sleep 5000) :expensive)))

;; allows for succint code as compared to nested let form in the if form.
(if-let [res :truthy] res :falsey)


(defn triangle [n]
  (/ (* n (+ n 1)) 2))

(defn inf-triangles [n]
  {:head (triangle n)
   :tail (delay (inf-triangles (inc n)))})
(defn head  [l]   (:head l))
(defn tail  [l]   (force (:tail l)))

(head (inf-triangles 2))
(tail (inf-triangles 100))

(defn rand-ints [n]
  (repeatedly n #(rand-int n)))

;; quick sort with lazy partitions.
;;
;; basic steps:
;; 1. Partition: In this phase, the algorithm figures out the correct sorted
;;    position for a 'pivot' element. Once the 'pivot' element is at the correct
;;    position, the elements on its left are smaller and its right are bigger
;;    than itself.
;; 2. Recurrence: Now, we can apply quick sort to left and right partitions.
;;
;; What do lazy partitions optimise?
;; It only computes the correct sorted position for the element at a given index
;; (including the partially sorted list in this intermediary state). All other
;; computations can be delayed.
(defn sort-parts [partitions]
  (lazy-seq
   (loop [[current & remaining] partitions]
     (if-let [[pivot & others] (seq current)]
       (let [smaller? #(< % pivot)]
         ; Because of immutability, we need to reconstruct a new sequence with
         ; correct position for the pivot element instead of modifying it
         ; in-place. This is where we differ from traditional quick-sort
         ; implementations.
         ;
         ; (list* '(0 1) 2 '(3 4) '(5 6)) ;=> ((0 1) 2 (3 4) 5 6)
         ;
         ; How they came up with destructuring strategy for this complicated
         ; nested list is still unclear, but I've spent enough time on this
         ; already. (ㆆ_ㆆ)
         (recur (list*
                 (filter smaller? others)
                 pivot
                 (remove smaller? others)
                 remaining)))
       (when-let [[x & parts] remaining]
         (cons x (sort-parts parts)))))))

(defn quick-sort [items]
  (sort-parts (list items)))

(quick-sort (rand-ints 10))
