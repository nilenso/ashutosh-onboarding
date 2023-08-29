(ns clojure-sandbox.chapter-9
  (:refer-clojure :exclude [defstruct]) ; exclude defstruct from clojure.core
  (:use (clojure set xml)) ; import everything in clojure.set and clojure.xml without ns qualification.
  (:use [clojure.test :only (are is)]) ; import only are and is functions form clojure.test
  (:require (clojure [zip :as z])) ; import clojure.zip but alias it to z.
  (:import (java.util Date)
           (java.io File))) ; import java classes Date and File.

(in-ns 'universe-1)
(def greeting "Hello, world!")

(in-ns 'clojure-sandbox.chapter-9)
(refer 'universe-1 :only '[greeting])
`(~greeting)


(def b (create-ns 'universe-2))
b

((ns-map b) 'String)
(intern b 'x 9)
universe-2/x

;; Universal Design Pattern (UDP)
(defn beget [this proto]
  (assoc this ::prototype proto))

(beget {:a 2} {:a 1})

(defn get* [m k]
  (when m
    (if-let [[_ v] (find m k)]
      v
      (recur (get m ::prototype) k))))

(get* (beget {:a nil} {:a 1}) :a)
(get* (beget {:a 2} {:a 1}) :a)

(def put assoc)
(put (beget {} nil) :a 1)

(def a-cat {:likes-dogs true :ocd-bathing true})
(def morris-the-cat (beget {:name "morris"} a-cat))
(def morris-with-ptsd (beget {:likes-dogs nil} morris-the-cat))

(get* a-cat :likes-dogs)
(get* morris-the-cat :likes-dogs)
(get* morris-with-ptsd :likes-dogs)

(defmulti walk (fn [c] (get* c :likes-dogs)))
(defmethod walk true [_] "walk")
(defmethod walk nil [_] "run")

(walk a-cat)
(walk morris-the-cat)
(walk morris-with-ptsd)
