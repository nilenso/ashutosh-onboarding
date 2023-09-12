(ns clojure-sandbox.chapter-17
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

(def artists
  #{{:artist "Burial"  :genre-id 1}
    {:artist "Magma" :genre-id 2}
    {:artist "Can" :genre-id 3}
    {:artist "Faust" :genre-id 3}
    {:artist "Ikonika" :genre-id 1}
    {:artist "Grouper"}})

(def genres
  #{{:genre-id 1 :genre-name "Dubstep"}
    {:genre-id 2 :genre-name "Zeuhl"}
    {:genre-id 3 :genre-name "Prog"}
    {:genre-id 4 :genre-name "Drone"}})

(require '[clojure.set :as s])

(def ALL identity)
(defn ids [& ids]
  (fn [m]
    ((set ids) (:genre-id m))))

(s/select ALL genres)
(s/select (ids 2 3) genres)
(take 2 (s/select ALL (s/join artists genres)))

(defn shuffle-expr [expr]
  (if (coll? expr)
    (if (= (first expr) `unquote)
      "?"
      (let [[op & args] expr]
        (str "("
             (str/join (str " " op " ")
                       (map shuffle-expr args))
             ")")))
    expr))

(shuffle-expr '(= X.a Y.b))
(shuffle-expr '(AND (< a 5) (< b ~max)))


(defn process-where-clause [processor expr]
  (str " WHERE " (processor expr)))


(process-where-clause shuffle-expr '(AND (< a 5) (< b ~max)))

(defn process-left-join-clause [processor table _ expr]
  (str " LEFT JOIN " table
       " ON " (processor expr)))

(apply process-left-join-clause
       shuffle-expr
       '(Y :ON (= X.a Y.b)))


(let [LEFT-JOIN (partial process-left-join-clause shuffle-expr)]
  (LEFT-JOIN 'Y :ON '(= X.a Y.b)))

(defn process-from-clause [processor table & joins]
  (apply str " FROM " table
         (map processor joins)))

(process-from-clause shuffle-expr 'X
                     (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))

(defn process-select-clause [processor fields & clauses]
  (apply str "SELECT " (str/join ", " fields)
         (map processor clauses)))

(process-select-clause shuffle-expr
                       '[a b c]
                       (process-from-clause
                        shuffle-expr
                        'X
                        (process-left-join-clause shuffle-expr 'Y :ON '(= X.a Y.b)))
                       (process-where-clause shuffle-expr '(AND (< a 5) (< b ~max))))

(declare apply-syntax)
(def ^:dynamic *clause-map*
  {'SELECT    (partial process-select-clause apply-syntax)
   'FROM      (partial process-from-clause apply-syntax)
   'LEFT-JOIN (partial process-left-join-clause shuffle-expr)
   'WHERE     (partial process-where-clause shuffle-expr)})

(defn apply-syntax [[op & args]]
  (apply (get *clause-map* op) args))

(defmacro SELECT [& args]
  {:query (apply-syntax (cons 'SELECT args))
   :bindings (vec (for [n (tree-seq coll? seq args)
                        :when (and (coll? n)
                                   (= (first n) `unquote))]
                    (second n)))})

(macroexpand '(SELECT [a b c] (FROM X)))

(defn example-query [max]
  (SELECT [a b c]
          (FROM X
                (LEFT-JOIN Y :ON (= X.a Y.b)))
          (WHERE (AND (< a 5) (< b ~max)))))

(example-query 5)


(def stubbed-feed-children
  (constantly [{:content [{:tag :title
                           :content ["Stub"]}]}]))

(use '[clojure-sandbox.chapter-11 :only [feed-children title occurrences count-text-task]])

(defn count-feed-entries [url]
  (count (feed-children url)))

(comment (count-feed-entries "http://blog.fogus.me/feed/"))

(with-redefs [feed-children stubbed-feed-children]
  (count-feed-entries "dummy url"))

(with-redefs [feed-children stubbed-feed-children]
  (occurrences title "Stub" "a" "b" "c"))

(require '[clojure.test :refer (deftest testing is)])

(deftest feed-tests
  (with-redefs [feed-children stubbed-feed-children]
    (testing "Child Counting"
      (is (= 1000 (count-feed-entries "Dummy URL"))))
    (testing "Occurrence Counting"
      (is (= 0 (count-text-task
                title
                "ZOMG"
                "Dummy URL"))))))

(comment (clojure.test/run-tests 'clojure-sandbox.chapter-17))


(def config
  '{:systems {:pump {:type :feeder, :descr "Feeder system"}
              :sim1 {:type :sim,    :fidelity :low}
              :sim2 {:type :sim,    :fidelity :high, :threads 2}}})

(defn describe-system [name cfg]
  [(:type cfg) (:fidelity cfg)])

(describe-system :pump {:type :feeder, :descr "Feeder system"})

(defmulti construct describe-system)

(defmethod construct :default [name cfg]
  {:name name
   :type (:type cfg)})

(defn construct-subsystems [sys-map]
  (for [[name cfg] sys-map]
    (construct name cfg)))

(construct-subsystems (:systems config))

(defmethod construct [:feeder nil]
  [_ cfg]
  (:descr cfg))

(construct-subsystems (:systems config))

(defrecord LowFiSim [name])
(defrecord HiFiSim  [name threads])

(defmethod construct [:sim :low]
  [name cfg]
  (->LowFiSim name))

(defmethod construct [:sim :high]
  [name cfg]
  (->HiFiSim name (:threads cfg)))

(construct-subsystems (:systems config))

(def lofi {:type :sim, :descr "Lowfi sim", :fidelity :low})
(def hifi {:type :sim, :descr "Hifi sim", :fidelity :high, :threads 2})

(construct :lofi lofi)

(defprotocol Sys
  (start! [sys])
  (stop! [sys]))

(defprotocol Sim
  (handle [sim msg]))

(defn build-system [name config]
  (let [sys (construct name config)]
    (start! sys)
    sys))

(extend-type LowFiSim
  Sys
  (start! [this]
    (println "Started a lofi simulator."))
  (stop!  [this]
    (println "Stopped a lofi simulator."))
  Sim
  (handle [this msg]
    (* (:weight msg) 3.14)))

(start! (construct :lofi lofi))

(build-system :sim1 lofi)

(handle (build-system :sim1 lofi) {:weight 42})


(defn traverse [node f]
  (when node
    (f node)
    (doseq [child (:content node)]
      (traverse child f))))

(traverse {:tag :flower :attrs {:name "Tanpopo"} :content []}
          println)

(refer '[clojure.xml :as xml])
(def DB
  (-> "<zoo>
         <pongo>
           <animal>orangutan</animal>
         </pongo>
         <panthera>
           <animal>Spot</animal>
           <animal>lion</animal>
           <animal>Lopshire</animal>
         </panthera>
       </zoo>"
      .getBytes
      (java.io.ByteArrayInputStream.)
      xml/parse))

(traverse DB println)

(defn ^:dynamic handle-weird-animal [{[name] :content}]
  (throw (Exception. (str name " must be 'dealt with'"))))

(defmulti visit :tag)
(defmethod visit :animal [{[name] :content :as animal}]
  (case name
    "Spot"     (handle-weird-animal animal)
    "Lopshire" (handle-weird-animal animal)
    (println name)))

(traverse DB visit)
