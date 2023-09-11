(ns clojure-sandbox.chapter-7)

(map [:a :b :c :d] [1 2])

(let [fourth (comp first rest rest rest rest)]
  (fourth (range)))

(letfn [(fnth [n]
          (apply comp
                 (cons first
                       (take (dec n) (repeat rest)))))]
  ((fnth 10) (range)))

((complement even?) 42)

(defn join
  {:test (fn []
           (assert
            (= (join "," [1 2 3]) "1,2,3")))}
  [sep s]
  (apply str (interpose sep s)))

(sort-by second [[:a 7], [:c 13], [:b 21]])


(def plays [{:band "Burial",     :plays 979,  :loved 9}
            {:band "Eno",        :plays 2333, :loved 15}
            {:band "Bill Evans", :plays 979,  :loved 9}
            {:band "Magma",      :plays 2665, :loved 31}])

(vec (map (plays 0) [:plays :loved :band]))

(defn columns [column-names]
  (fn [row] (vec (map #(% row) column-names))))

(sort-by (columns [:plays :loved :band]) plays)

(defn put-things [m]
  (into m {:meat "beef" :veggie "broccoli"}))

(defn balanced-diet [f m]
  {:post [(:meat %) (:veggie %)]}
  (f m))

(balanced-diet put-things {})

(defn finicky [f m]
  {:post [(= (:meat %) (:meat m))]}
  (f m))

;; will fail with AssertionError
(comment (finicky put-things {}))

(def add-and-get
  (let [ai (java.util.concurrent.atomic.AtomicInteger.)]
    (fn [y] (.addAndGet ai y))))

(add-and-get 3)
(add-and-get 1)
(add-and-get 6)

(defn filter-divisible [denom s]
  (filter (fn [num] (zero? (rem num denom))) s))

(filter-divisible 10 (range 100))

(def bearings [{:x  0, :y  1}
               {:x  1, :y  0}
               {:x  0, :y -1}
               {:x -1, :y  0}])

(defn bot [x y bearing-num]
  {:coords  [x y]
   :bearing ([:north :east :south :west] bearing-num)
   :forward (fn [] (bot (+ x (:x (bearings bearing-num)))
                        (+ y (:y  (bearings bearing-num)))
                        bearing-num))
   :turn-right (fn [] (bot x y (mod (+ 1 bearing-num) 4)))
   :turn-left  (fn [] (bot x y (mod (- 1 bearing-num) 4)))})

(:coords (bot 5 5 0))
(:bearing (bot 5 5 0))
(:coords ((:forward (bot 5 5 0))))
(:coords ((:forward ((:forward ((:turn-right (bot 5 5 0))))))))


(defn convert [context descriptor]
  (reduce
   (fn [result [mag unit]]
     (+ result (let [val (get context unit)]
                 (if (vector? val)
                   (* mag (convert context val))
                   (* mag val)))))
   0
   (partition 2 descriptor)))


(def simple-metric {:meter 1,
                    :km 1000,
                    :cm 1/100,
                    :mm [1/10 :cm]})
(float (convert simple-metric [3 :km 10 :meter 80 :cm 10 :mm]))

;; but convert is not bound to just units of length. we can do more
(convert {:bit 1, :byte 8, :nibble [1/2 :byte]} [32 :nibble])

(defn elevator [commands]
  (letfn
   [(ff-open [[_ & r]]
      "When the elevator is open on the 1st floor
      it can either close or be done."
      #(case _
         :close (ff-closed r)
         :done  true
         false))
    (ff-closed [[_ & r]]
      "When the elevator is closed on the 1st floor
      it can either open or go up."
      #(case _
         :open (ff-open r)
         :up   (sf-closed r)
         false))
    (sf-closed [[_ & r]]
      "When the elevator is closed on the 2nd floor
      it can either go down or open."
      #(case _
         :down (ff-closed r)
         :open (sf-open r)
         false))
    (sf-open [[_ & r]]
      "When the elevator is open on the 2nd floor
    it can either close or be done"
      #(case _
         :close (sf-closed r)
         :done  true
         false))]
    (trampoline ff-open commands)))

(elevator [:close :open :close :up :open :open :done])
(elevator [:close :up :open :close :down :open :done])

;; A* implementation
(defn neighbors
  "Finds valid neighbours of a cell in a matrix of size `size x size`"
  ([size yx] (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
                        size yx))
  ([deltas size yx]
   (filter (fn [new-yx]
             (every? #(< -1 % size) new-yx))
           (map #(vec (map + yx %))
                deltas))))

(defn estimate-cost
  "Estimates cost of travelling in straigh line. e.g. cost of travelling from
   `[0 0]` to `[4 4]` in a matrix of size `5x5` is equal to the cost of
   travelling 5 cells right and then 5 cells down."
  [step-cost-est size y x] (* step-cost-est (- (+ size size) y x 2)))

(defn path-cost
  "Cost of the path traversed so far."
  [node-cost cheapest-nbr] (+ node-cost (or (:cost cheapest-nbr) 0)))

(defn total-cost
  "Total estimated cost of the path"
  [newcost step-cost-est size y x] (+ newcost (estimate-cost step-cost-est size y x)))

(defn min-by [f coll]
  (when (seq coll)
    (reduce (fn [min other]
              (if (> (f min) (f other))
                other
                min))
            coll)))

(defn astar [start-yx step-est cell-costs]
  (let [size (count cell-costs)]
    (loop [steps 0
           routes (vec (repeat size (vec (repeat size nil)))) ; ((nil, nil, ...), ..., (nil, nil, ...))
           work-todo (sorted-set [0 start-yx])] ; #{[0 [0 1]] ...}
      (if (empty? work-todo)
        [(peek (peek routes)) :steps steps] ; last item from the last row of routes.
        (let [[_ yx :as work-item] (first work-todo) ; take the first work item
              rest-work-todo (disj work-todo work-item) ; take the rest work items
              nbr-yxs (neighbors size yx) ; find valid neighbors of current yx
              cheapest-nbr (min-by :cost (keep #(get-in routes %) nbr-yxs)) ; find cheapest neighbor
              newcost (path-cost (get-in cell-costs yx) cheapest-nbr) ; new path cost from start to current node
              oldcost (:cost (get-in routes yx))] ; old path cost from start to current node
          (if (and oldcost (>= newcost oldcost))
            (recur (inc steps) routes rest-work-todo) ; ignore new path
            (recur (inc steps) ; proceed while also considering the new path.
                   (assoc-in routes yx
                             {:cost newcost
                              :yxs (conj (:yxs cheapest-nbr [])
                                         yx)}) ; update the cheapest path from start to current node
                   (into rest-work-todo
                         (map
                          (fn [w]
                            (let [[y x] w]
                              [(total-cost newcost step-est size y x) w]))
                          nbr-yxs))))))))) ; add valid neighbours of the current node for further exploration.

(def world [[1 1 1 1 1]
            [999 999 999 999 1]
            [1 1 1 1 1]
            [1 999 999 999 999]
            [1 1 1 1 1]])

(astar [0 0]
       900
       world)
