(ns clojure-sandbox.chapter-12
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]))

(def OK java.net.HttpURLConnection/HTTP_OK)

(defn respond
  ([exchange body]
   (respond identity exchange body))
  ([around exchange body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))

(defn new-server [port path handler]
  (doto
   (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

(defn default-handler [txt]
  (proxy
   [HttpHandler]
   []
    (handle [exchange]
      (respond exchange txt))))

(def handler (default-handler "Hello, Six!"))
(def server (new-server 8123 "/" handler))

(.stop server 0)


(defn html-around [o]
  (proxy
   [FilterOutputStream]
   [o] (write [raw-bytes]
         (proxy-super write (.getBytes (str "<!doctype html>
<html>
<body>
" (String. raw-bytes) "</body>"))))))

;; updating handler proxy object.
(update-proxy handler {"handle" (fn [_ exchange]
                                  (let [headers (.getRequestHeaders exchange)]
                                    (respond html-around exchange (prn-str headers))))})

(defn listing [file]
  (-> file .list sort))

(listing (io/file "."))

(defn html-links [root filenames]
  (string/join
   (for [file filenames]
     (str "<a href='"
          (str root (if (= "/" root) "" File/separator) file)
          "'>"
          file
          "</a><br>\n"))))

(html-links "." (listing (io/file ".")))

(defn details [file]
  (str (.getName file) " is "
       (.length file)  " bytes."))

(details (io/file "./project.clj"))

(defn uri->file [root uri]
  (->> uri
       str
       URLDecoder/decode
       (str root)
       io/file))

(uri->file "." (URI. "/project.clj"))
(details (uri->file "." (URI. "/project.clj")))

(def fs-handler
  (fn [_ exchange]
    (let [uri (.getRequestURI exchange)
          file (uri->file "." uri)]
      (if (.isDirectory file)
        (do
          (.add
           (.getResponseHeaders exchange)
           "Content-Type"
           "text/html")
          (respond
           html-around
           exchange
           (html-links (str uri) (listing file))))
        (respond exchange (details file))))))

(update-proxy handler {"handle" fs-handler})

(comment (gui.DynaFrame. "test"))

(comment (meta (gui.DynaFrame. "3rd")))
(comment (gui.DynaFrame/version))

(comment (def gui (gui.DynaFrame. "4th")))
(comment
  (.display
   gui
   (doto (javax.swing.JPanel.)
     (.add (javax.swing.JLabel. "Charlemagne and Pippin")))))

(comment
  (.display
   gui
   (doto (javax.swing.JPanel.)
     (.add (javax.swing.JLabel. "Mater semper certa est.")))))

(import
 '(gui DynaFrame)
 '(javax.swing Box BoxLayout JTextField JPanel
               JSplitPane JLabel JButton
               JOptionPane)
 '(java.awt BorderLayout Component GridLayout FlowLayout)
 '(java.awt.event ActionListener))

(defn shelf [& components]
  (let [shelf (JPanel.)]
    (.setLayout shelf (FlowLayout.))
    (doseq [c components] (.add shelf c))
    shelf))

(defn stack [& components]
  (let [stack (Box. BoxLayout/PAGE_AXIS)]
    (doseq [c components]
      (.setAlignmentX c Component/CENTER_ALIGNMENT)
      (.add stack c))
    stack))

(defn splitter [top bottom]
  (doto (JSplitPane.)
    (.setOrientation JSplitPane/VERTICAL_SPLIT)
    (.setLeftComponent top)
    (.setRightComponent bottom)))

(defn button [text f]
  (doto (JButton. text)
    (.addActionListener
     (proxy [ActionListener] []
       (actionPerformed [_] (f))))))

(defn txt  [cols t]
  (doto (JTextField.)
    (.setColumns cols)
    (.setText t)))

(defn label [txt] (JLabel. txt))

(defn alert
  ([msg] (alert nil msg))
  ([frame msg]
   (javax.swing.JOptionPane/showMessageDialog frame msg)))


(comment
  (let [f (gui.DynaFrame. "One")]
    (.display
     f
     (splitter
      (button "Procrastinate" #(alert "Eat Cheetos"))
      (button "Move It" #(alert "Couch to 5k"))))))

(defn grid [x y f]
  (let [g (doto (JPanel.)
            (.setLayout (GridLayout. x y)))]
    (dotimes [_ x]
      (dotimes [_ y]
        (.add g (f))))
    g))

(comment
  (let [f (gui.DynaFrame. "Two")
        g1 (txt 10 "Charlemagne")
        g2 (txt 10 "Pippin")
        r  (txt 3 "10")
        d  (txt 3 "5")]
    (.display
     f
     (splitter
      (stack
       (shelf (label "Player 1") g1)
       (shelf (label "Player 2") g2)
       (shelf (label "Rounds ") r
              (label "Delay  ") d))
      (stack
       (grid 21 11 #(label "-"))
       (button "Go!" #(alert (str (.getText g1) " vs. "
                                  (.getText g2) " for "
                                  (.getText r)  " rounds, every "
                                  (.getText d)  " seconds."))))))))


;; into-array builds an array of boxed types (Character class instead of char primitive).
(doto (StringBuilder. "abc")
  (.append (into-array [\x \y \z])))

(doto (StringBuilder. "abc")
  (.append (char-array [\x \y \z])))

(let [ary (make-array Long/TYPE 3 3)]
  (dotimes [i 3]
    (dotimes [j 3]
      (aset ary i j (+ i j))))
  (map seq ary))

(type (aget (into-array Integer/TYPE [1 2 3 4]) 0))
(type (into-array Integer/TYPE [1 2 3 4])) ; page 295.

;; Note: Java arrays are mutable.
(def ary  (into-array [1 2 3]))
(def sary (seq ary))
sary

(aset ary 0 42)
sary

(defmulti what-is class)
(defmethod what-is
  (Class/forName "[Ljava.lang.String;")
  [_]
  "1d String")

(defmethod what-is
  (Class/forName "[[Ljava.lang.Object;")
  [_]
  "2d Object")

(defmethod what-is
  (Class/forName "[[[[I")
  [_]
  "Primitive 4d int")

(what-is (into-array ["a" "b"]))
(what-is (to-array-2d [[1 2] [3 4]]))
(what-is (make-array Integer/TYPE 2 2 2 2))

(String/format "%d %f" (into-array Number [1 1.0]))

(ancestors (class #()))

(import '[java.util Comparator Collections ArrayList])
(defn gimme [] (ArrayList. [1 3 4 8 2]))
(doto (gimme)
  (Collections/sort #(- %1 %2))) ;; all functions implement comparators

(doto (gimme)
  (Collections/sort
   (reify Comparator
     (compare [_ l r]
       (cond
         (> l r) -1
         (= l r) 0
         :else 1)))))

(doto (gimme) (Collections/sort >))
(doto (gimme) (Collections/sort <))
(doto (gimme) (Collections/sort (complement #(- %1 %2))))

(comment (doto (Thread. #(do (Thread/sleep 5000)
                             (println "haikeeba!")))
           .start))

(import '[java.util.concurrent FutureTask])
(let [f (FutureTask. #(do (Thread/sleep 2000) 42))]
  (.start (Thread. #(.run f)))
  (.get f))


(ancestors (class {}))
(ancestors (class []))
(ancestors (class #{}))
(ancestors (class (repeat :a)))

(defn shuffle [coll]
  (seq (doto (java.util.ArrayList. coll)
         java.util.Collections/shuffle)))
(shuffle (range 10))

(definterface ISliceable
  (slice [^long s ^long e])
  (^long sliceCount []))

(def dumb
  (reify clojure_sandbox.chapter_12.ISliceable
    (slice [_ s e] [:empty])
    (sliceCount [_] 42)))
(.slice dumb 1 2)
(.sliceCount dumb)

;; using a protocol to extend an interface. This could be useful when extending
;; final Java classes.
(defprotocol Sliceable
  (slice [this s e])
  (sliceCount [this]))

(extend ISliceable
  Sliceable
  {:slice (fn [this s e] (.slice this s e))
   :sliceCount (fn [this] (.sliceCount this))})

(defn calc-slice-count
  "Calculates the number of possible slices using the formula:
       (n + r - 1)!
       ------------
        r!(n - 1)!
     where n is (count thing) and r is 2"
  [thing]
  (let [! #(reduce * (take % (iterate inc 1)))
        n (count thing)] (/ (! (- (+ n 2) 1))
                            (* (! 2) (! (- n 1))))))

(extend-type String
  Sliceable
  (slice [this s e] (.substring this s (inc e)))
  (sliceCount [this] (calc-slice-count this)))

(slice "abc" 0 1)
(sliceCount "abc")
