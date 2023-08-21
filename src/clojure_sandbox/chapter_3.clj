(ns clojure-sandbox.chapter-3)

;; evil-false
(def evil-false (Boolean. false))

evil-false ;; => false
(= false evil-false) ;; => true
(if evil-false :truthy :falsey) ;; => :truthy

;; exclusive checks to confirm `nil` or `false`
(nil? nil)
(false? nil)
(nil? false)
(false? false)

;; rest always returns a sequence. next returns `(seq (rest s))`.
(rest [1 2 3])
(next [1 2 3])
(rest [1]) ; => '()
(next [1]) ; => nil

;; doseq
(doseq [x [1 2 3]]
  (prn x))

;; map destructuring
(def guys-name-map
  {:f-name "Guy" :m-name "Lewis" :l-name "Steele"})
(let [{f-name :f-name, l-name :l-name, m-name :m-name} guys-name-map]
  (str l-name ", " f-name " " m-name))

(let [{:keys [f-name m-name l-name title] :or {title  "Mr"} :as everything} guys-name-map]
  (str title ", " l-name ", " f-name " " m-name " " everything))

;; named function args
(defn whole-name [& {:keys [f-name m-name l-name title] :or {title "Mr"}}]
  (str title ", " l-name ", " f-name " " m-name))

(whole-name :f-name "Guy" :m-name "Lewis" :l-name "Steele")

(defn f-values [f max-x max-y]
  (for [x (range max-x)
        y (range max-y)]
    [x y (f x y)]))

;; def for REPL interactions
(def frame (java.awt.Frame. "Function Values"))
(defn draw-values [f xs ys]
  (.setSize frame (java.awt.Dimension. xs ys))
  (.show frame) ; frame needs to be visible for graphics to be non-null
  (let [gfx (.getGraphics frame)]
    (.clearRect gfx 0 0 xs ys)
    (doseq [[x y v] (f-values f xs ys)]
      (let [color (rem v 256)]
        (.setColor gfx (java.awt.Color. color color color))
        (.fillRect gfx x y 1 1)))))

