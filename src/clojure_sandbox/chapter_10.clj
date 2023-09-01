(ns clojure-sandbox.chapter-10
  (:import java.util.concurrent.Executors)
  (:refer-clojure :exclude [aget aset count seq])
  (:require [clojure.core :as clj]))

(def initial-board
  [[:- :k :-]
   [:- :- :-]
   [:- :K :-]])

(defn board-map [f board]
  (vec (map #(vec (for [s %] (f s)))
            board)))

(defn reset-board!
  "Resets the board state.  Generally these types of functions are a
   bad idea, but matters of page count force our hand."
  []
  (def board (board-map ref initial-board))
  (def to-move (ref [[:K [2 1]] [:k [0 1]]]))
  (def num-moves (ref 0)))

(defn neighbors
  "Finds valid neighbours of a cell in a matrix of size `size x size`"
  ([size yx] (neighbors [[-1 0] [1 0] [0 -1] [0 1]]
                        size yx))
  ([deltas size yx]
   (filter (fn [new-yx]
             (every? #(< -1 % size) new-yx))
           (map #(vec (map + yx %))
                deltas))))

(def king-moves
  (partial neighbors
           [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]] 3))

(defn good-move?
  [to enemy-sq]
  (when (not= to enemy-sq)
    to))

(defn choose-move
  "Randomly choose a legal move"
  [[[mover mpos] [_ enemy-pos]]]
  [mover (some #(good-move? % enemy-pos)
               (shuffle (king-moves mpos)))])

(take 5 (repeatedly #(choose-move @to-move)))

(defn place [from to] to)

(defn move-piece [[piece dest] [[_ src] _]]
  (alter (get-in board dest) place piece)
  (alter (get-in board src) place :-)
  (alter num-moves inc))

(defn update-to-move [move]
  (alter to-move #(vector (second %) move)))

(defn make-move []
  (let [move (choose-move @to-move)]
    (dosync (move-piece move @to-move))
    (dosync (update-to-move move))))

(make-move)
(board-map deref board)

(def thread-pool
  (Executors/newFixedThreadPool
   (+ 2 (.availableProcessors (Runtime/getRuntime)))))

(defn dothreads! [f & {thread-count :threads
                       exec-count :times :or {thread-count 1 exec-count 1}}]
  (dotimes [_ thread-count]
    (.submit thread-pool
             #(dotimes [_ exec-count] (f)))))

(dothreads! make-move :threads 100 :times 100)
(board-map deref board) ; corrupted board

(defn make-move []
  (dosync
   (let [move (choose-move @to-move)]
     (move-piece move @to-move)
     (update-to-move move))))

(reset-board!)
(dothreads! make-move :threads 100 :times 1000)
(board-map #(dosync (deref %)) board)
@num-moves
@to-move


(defn stress-ref [r]
  (let [slow-tries (atom 0)]
    (future
      (dosync
       (swap! slow-tries inc)
       (Thread/sleep 200)
       @r)
      (println (format "r is: %s, history: %d, after: %d tries"
                       @r (.getHistoryCount r) @slow-tries)))
    (dotimes [i 500]
      (Thread/sleep 10)
      (dosync (alter r inc)))
    :done))

(stress-ref (ref 0 :max-history 30))
(stress-ref (ref 0 :min-history 15 :max-history 30))

(def x (agent []))
(send x conj 0)
(send x conj 1)
@x

(defn slow-conj [coll item]
  (Thread/sleep 1000)
  (conj coll item))

(send x slow-conj 33)
@x

(def log-agent (agent 0))
(defn log-msg [msg-id message]
  (println msg-id message)
  (inc msg-id))

(dothreads! #(send log-agent log-msg "hello") :threads 100 :count 1000)
(defn do-step [channel message]
  (Thread/sleep 1)
  (send-off log-agent log-msg (str channel message)))


(defn three-step [channel]
  (do-step channel " ready to begin (step 0)")
  (do-step channel " warming up (step 1)")
  (do-step channel " really getting going now (step 2)")
  (do-step channel " done! (step 3)"))

(defn all-together-now []
  (dothreads! #(three-step "alpha"))
  (dothreads! #(three-step "beta"))
  (dothreads! #(three-step "omega")))

(three-step "1")
(all-together-now)

(send-off log-agent (fn [_] (throw (Exception. "agent encountered an error"))))
(agent-error log-agent)
(comment (send-off log-agent log-msg "test"))
(restart-agent log-agent 200)
(send-off log-agent log-msg "test")

(def another-agent (agent 0 :error-handler (fn [a e] (prn a e))))
(send-off another-agent (fn [_] (throw (Exception. "agent encountered an error"))))
(send-off another-agent log-msg "test")


(def ^:dynamic *time* (atom 0))
(defn tick [] (swap! *time* inc))
(dothreads! tick :threads 1000 :times 1000)
@*time*

(defn manipulable-memoize [function]
  (let [cache (atom {})]
    (with-meta
      (fn [& args]
        (or (second (find @cache args))
            (let [ret (apply function args)]
              (swap! cache assoc args ret)
              ret)))
      {:cache cache})))

(def slowly (fn [x] (Thread/sleep 1000) x))
(time [(slowly 9) (slowly 9)])

(def sometimes-slowly (manipulable-memoize slowly))
(time [(sometimes-slowly 108) (sometimes-slowly 108)])
(meta sometimes-slowly)

(let [cache (:cache (meta sometimes-slowly))]
  (swap! cache dissoc '(108)))

(defprotocol SafeArray
  (aset  [this i f])
  (aget  [this i])
  (count [this])
  (seq   [this]))


(defn make-dumb-array [t sz]
  (let [a (make-array t sz)]
    (reify
      SafeArray
      (seq [_] (clj/seq a))
      (count [_] (clj/count a))
      (aget [_ i] (clj/aget a i))
      (aset [this i f] (clj/aset a i (f (aget this i)))))))

(defn pummel [a]
  (dothreads! #(dotimes [i (count a)] (aset a i inc))
              :threads 100))

(def D (make-dumb-array Integer/TYPE 8))
(pummel D)
;; 100 threads incrementing each slot concurrently should return an array of 100s.
;; but it doesn't.
(seq D)

(defn make-safe-array [t sz]
  (let [a (make-array t sz)]
    (reify
      SafeArray
      (seq [_] (clj/seq a))
      (count [_] (clj/count a))
      (aget [_ i] (locking a (clj/aget a i)))
      (aset [this i f] (locking a (clj/aset a i (f (aget this i))))))))

(def S (make-safe-array Integer/TYPE 8))
(pummel S)
(seq S)

*read-eval*
(var *read-eval*)

(defn print-read-eval []
  (println "*read-eval* is currently" *read-eval*))

(defn binding-play []
  (print-read-eval)
  (binding [*read-eval* false]
    (print-read-eval))
  (print-read-eval))

(binding-play)

(def x 42)
{:outer-var-value x
 :with-locals (with-local-vars [x 9]
                {:local-var x
                 :local-var-value (var-get x)})}

(with-precision 4 (/ 1M 3))

(comment (/ 1M 3))
;; following errors because map is lazy the scope of with-precision is limited.
(comment (with-precision 4
           (map (fn [x] (/ x 3)) (range 1M 4M))))

;; we can mitigate this with doall
(comment (with-precision 4
           (doall (map (fn [x] (/ x 3)) (range 1M 4M)))))

;; or with bound-fn
(comment (with-precision 4
           (map (bound-fn [x] (/ x 3)) (range 1M 4M))))
