(ns clojure-sandbox.chapter-14
  (:require [clojure-sandbox.chapter-7 :as chapter-7]
            [clojure.edn :as edn]))

(def ascii (map char (range 65 (+ 65 26))))

(defn rand-str [sz alphabet]
  (apply str (repeatedly sz #(rand-nth alphabet))))

(rand-str 10 ascii)

(def rand-sym #(symbol (rand-str %1 %2)))
(def rand-key #(keyword (rand-str %1 %2)))

(rand-key 10 ascii)
(rand-sym 10 ascii)

(defn rand-vec [& generators]
  (into [] (map #(%) generators)))

(rand-vec #(rand-sym 5  ascii)
          #(rand-key 10 ascii)
          #(rand-int 1024))

(defn rand-map [sz kgen vgen]
  (into {}
        (repeatedly sz #(rand-vec kgen vgen))))

(rand-map 3 #(rand-key 5 ascii) #(rand-int 100))

(use 'clojure.data)

(diff [1 2 3] [1 2 4])

(def distance-reader
  (partial chapter-7/convert {:m  1
                              :km 1000,
                              :cm 1/100,
                              :mm [1/10 :cm]}))

#unit/length [1 :km]

(def time-reader
  (partial chapter-7/convert
           {:sec 1
            :min 60,
            :hr  [60 :min],
            :day [24 :hr]}))

(binding [*data-readers* {'unit/time #'clojure-sandbox.chapter-14/time-reader}]
  (read-string "#unit/time [1 :min 30 :sec]"))

;; catch all for non-existing data reader tags
(binding [*default-data-reader-fn* #(-> {:tag %1 :payload %2})]
  (read-string "#nope [:doesnt-exist]"))

(def T {'unit/time #'clojure-sandbox.chapter-14/time-reader})
(edn/read-string {:readers T} "#unit/time [1 :min 30 :sec]")

(edn/read-string {:readers T, :default vector} "#what/the :huh?")


(defn valid? [event]
  (boolean (get event :result)))

(valid? {})
(valid? {:result 42})

(defn effect [{:keys [ab h] :or {ab 0, h 0}}
              event]
  (let [ab  (inc ab)
        h   (if (= :hit (:result event))
              (inc h)
              h)
        avg (double (/ h ab))]
    {:ab ab :h h :avg avg}))

(effect {} {:result :hit})
(effect {:ab 599 :h 180}
        {:result :out})

(defn apply-effect [state event]
  (if (valid? event)
    (effect state event)
    state))

(apply-effect {:ab 600 :h 180 :avg 0.3}
              {:result :hit})

(def effect-all #(reduce apply-effect %1 %2))

(effect-all {:ab 0, :h 0}
            [{:result :hit}
             {:result :out}
             {:result :hit}
             {:result :out}])

(def events (repeatedly 100
                        (fn []
                          (rand-map 1
                                    #(-> :result)
                                    #(if (< (rand-int 10) 3)
                                       :hit
                                       :out)))))

(effect-all {} events)
(effect-all {} (take 50 events))

(def fx-timeline #(reductions apply-effect %1 %2))
(fx-timeline {} (take 3 events))


(require '[clojure.set :as sql])

(def PLAYERS #{{:player "Nick", :ability 32/100}
               {:player "Matt", :ability 26/100}
               {:player "Ryan", :ability 19/100}})

(defn lookup [db name]
  (first (sql/select #(= name (:player %)) db))) ; Pretend SQL data source

(lookup PLAYERS "Nick")

(defn update-stats [db event]
  (let [player    (lookup db (:player event))
        less-db   (sql/difference db #{player})]
    (conj less-db
          (merge player (effect player event)))))

(update-stats PLAYERS {:player "Nick", :result :hit})

(defn commit-event [db event]
  (dosync (alter db update-stats event)))

(commit-event (ref PLAYERS) {:player "Nick", :result :hit})

(defn rand-event [{ability :ability}]
  (let [able (numerator ability)
        max  (denominator ability)]
    (rand-map 1
              #(-> :result)
              #(if (< (rand-int max) able)
                 :hit
                 :out))))

(defn rand-events [total player]
  (take total
        (repeatedly #(assoc (rand-event player)
                            :player
                            (:player player)))))

(rand-events 3 {:player "Nick", :ability 32/100})

(def agent-for-player
  (memoize
   (fn [player-name]
     (-> (agent []
                :error-handler #(println "ERROR: " %1 %2)
                :error-mode :fail)))))

(defn feed [db event]
  (let [a (agent-for-player (:player event))]
    (send a
          (fn [state]
            (commit-event db event)
            (conj state event)))))

(defn feed-all [db events]
  (doseq [event events]
    (feed db event))
  db)

(let [db (ref PLAYERS)]
  (feed-all db (rand-events 100 {:player "Nick", :ability 32/100}))
  db)

(count @(agent-for-player "Nick"))
(effect-all {} @(agent-for-player "Nick"))

(defn simulate [total players]
  (let [events (apply interleave
                      (for [player players]
                        (rand-events total player)))
        results (feed-all (ref players) events)]
    (apply await (map #(agent-for-player (:player %)) players))
    @results))

(simulate 2 PLAYERS)
(simulate 400 PLAYERS)

(effect-all {} @(agent-for-player "Nick"))

(defn relative-units [context unit]
  (if-let [spec (get context unit)]
    (if (vector? spec)
      (chapter-7/convert context spec)
      spec)
    (throw (RuntimeException. (str "Undefined unit " unit)))))

(defmacro defunits-of [name base-unit & conversions]
  (let [magnitude (gensym)
        unit (gensym)
        units-map (into `{~base-unit 1}
                        (map vec (partition 2 conversions)))]
    `(defmacro ~(symbol (str "unit-of-" name))
       [~magnitude ~unit]
       `(* ~~magnitude
           ~(case ~unit
              ~@(mapcat
                 (fn [[u# & r#]]
                   `[~u# ~(relative-units units-map u#)])
                 units-map))))))

(defunits-of distance :m :km 1000 :cm 1/100 :mm [1/10 :cm])

(macroexpand '(defunits-of distance :m {:m  1
                                        :km 1000
                                        :cm 1/100
                                        :mm [1/10 :cm]}))

(unit-of-distance 1 :cm)
