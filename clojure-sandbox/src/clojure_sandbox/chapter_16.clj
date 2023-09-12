(ns clojure-sandbox.chapter-16
  (:require [clojure.set :as set]
            [clojure.core.logic :as logic]))

(def b1 '[3 - - - - 5 - 1 -
          - 7 - - - 6 - 3 -
          1 - - - 9 - - - -
          7 - 8 - - - - 9 -
          9 - - 4 - 8 - - 2
          - 6 - - - - 5 - 1
          - - - - 4 - - - 6
          - 4 - 7 - - - 2 -
          - 2 - 6 - - - - 3])

(defn prep [board]
  (map #(partition 3 %)
       (partition 9 board)))

(prep b1)

(defn print-board [board]
  (let [row-sep (apply str (repeat 37 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "   ")))
        (print (str (last subrow) " | ")))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

(-> b1 prep print-board)

(defn rows [board sz]
  (partition sz board))

(rows b1 9)

(defn row-for [board index sz]
  (nth (rows board sz) (/ index sz)))

(row-for b1 19 9)

(defn column-for [board index sz]
  (let [col (mod index sz)]
    (map #(nth % col)
         (rows board sz))))

(column-for b1 2 9)

(defn subgrid-for [board i]
  (let [rows (rows board 9)
        sgcol (/ (mod i 9) 3)
        sgrow (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) sgcol 3)
        grp (take 3 (drop (* 3 (int sgrow)) grp-col))]
    (flatten grp)))

(subgrid-for b1 10)

(defn numbers-present-for [board i]
  (set
   (concat (row-for board i 9)
           (column-for board i 9)
           (subgrid-for board i))))

(numbers-present-for b1 10)

(defn possible-placements [board i]
  (set/difference #{1 2 3 4 5 6 7 8 9} (numbers-present-for board i)))

(defn positions
  [pred coll]
  (keep-indexed (fn [idx x]
                  (when (pred x)
                    idx))
                coll))

(defn solve [board]
  (if-let [[i & _]
           (and (some '#{-} board)
                (positions  '#{-} board))]
    (flatten (map #(solve (assoc board i %))
                  (possible-placements board i)))
    board))

(time (-> b1
          solve
          prep
          print-board))

(defn lvar?
  "Determines if a value represents a logic variable"
  [x]
  (boolean
   (when (symbol? x)
     (re-matches #"^\?.*" (name x)))))

(lvar? '?x)

(defn satisfy1
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      :else nil)))

(satisfy1 '?a 2 {})

(->> {}
     (satisfy1 '?x '?y)
     (satisfy1 '?x 1))

(defn satisfy [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (not knowledge) nil
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      (every? seq? [L R]) (satisfy
                           (rest L)
                           (rest R)
                           (satisfy (first L)
                                    (first R)
                                    knowledge))
      :else nil)))

(satisfy '(1 2 3) '(1 ?something 3) {})
(satisfy '(1 4 ?another-thing) '(1 ?something 3) {})
(satisfy '(?z ?x ?y) '(1 2 ?z) {})

(require '[clojure.walk :as walk])
(defn subst [term binds]
  (walk/prewalk
   (fn [expr]
     (if (lvar? expr)
       (or (binds expr) expr)
       expr))
   term))

(subst '(1 ?x 3) '{?x 2})
(let [L '(?z ?x ?y)
      R '(1 2 ?z)
      knowledge (satisfy L R {})]
  (subst L knowledge))

(defn meld [term1 term2]
  (->> {}
       (satisfy term1 term2)
       (subst term1)))

(meld '(1 ?x 3) '(1 2 ?y))
(meld '(1 ?x) '(?y (?y 2)))

(satisfy '?x 1 (satisfy '?x '?y {}))
(satisfy '(1 ?x) '(?y (?y 2)) {})

(require '[clojure.core.logic :as logic])

(logic/run* [answer]
            (logic/== answer 5))

(logic/run* [val1 val2]
            (logic/== {:a val1, :b 2}
                      {:a 1,    :b val2}))

(logic/run* [q]
            (logic/== q 1)
            (logic/== q 2))

(logic/run* [x y]
            (logic/== x y))

(logic/run* [george]
            (logic/conde
             [(logic/== george :born)]
             [(logic/== george :unborn)]))

(logic/run* [q]
            (logic/fresh [x y]
                         (logic/== q [x y])
                         (logic/!= y "Java")))
(logic/run* [q]
            (logic/fresh [x y]
                         (logic/== [:pizza "Java"] [x y])
                         (logic/== q [x y])
                         (logic/!= y "Java")))

(logic/run* [q]
            (logic/fresh [x y]
                         (logic/== [:pizza "Scala"] [x y])
                         (logic/== q [x y])
                         (logic/!= y "Java")))

(logic/run* [q]
            (logic/fresh [n]
                         (logic/!= 1 n)
                         (logic/== q n)))

(require '[clojure.core.logic.fd :as fd])

(logic/run* [q]
            (logic/fresh [n]
                         (fd/in n (apply fd/domain (range 10)))
                         (fd/in q (apply fd/domain (range 5)))
                         (logic/== q n)))

(defn rowify [board]
  (->> board
       (partition 9)
       (map vec)
       vec))

(rowify b1)

(defn colify [rows]
  (apply map vector rows))

(colify (rowify b1))


(defn subgrid [rows]
  (partition 9
             (for [row (range 0 9 3)
                   col (range 0 9 3)
                   x (range row (+ row 3))
                   y (range col (+ col 3))]
               (get-in rows [x y]))))

(subgrid (rowify b1))

(def logic-board #(repeatedly 81 logic/lvar))

(defn init [[lv & lvs] [cell & cells]]
  (if lv
    (logic/fresh []
                 (if (= '- cell)
                   logic/succeed
                   (logic/== lv cell))
                 (init lvs cells))
    logic/succeed))

(defn solve-logically [board]
  (let [legal-nums (fd/interval 1 9)
        lvars (logic-board)
        rows  (rowify lvars)
        cols  (colify rows)
        grids (subgrid rows)]
    (logic/run 1 [q]
               (init lvars board)
               (logic/everyg #(fd/in % legal-nums) lvars)
               (logic/everyg fd/distinct rows)
               (logic/everyg fd/distinct cols)
               (logic/everyg fd/distinct grids)
               (logic/== q lvars))))

(time (-> b1
     solve-logically
     first
     prep
     print-board))
