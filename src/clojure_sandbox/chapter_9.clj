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

(defmulti compiler :os)
(defmethod compiler ::unix [m] (get m :c-compiler))
(defmethod compiler ::osx  [m] (get m :llvm-compiler))

(def clone (partial beget {}))
(def unix   {:os ::unix, :c-compiler "cc", :home "/home", :dev "/dev"})
(def osx  (-> (clone unix)
              (put :os ::osx)
              (put :llvm-compiler "clang")
              (put :home "/Users")))

(compiler unix)
(compiler osx)

(defmulti home :os)
(defmethod home ::unix [m] (get m :home))

(home unix)
(comment (home osx))

(derive ::osx ::unix)
(home osx)

(parents ::osx)
(ancestors ::osx)
(descendants ::unix)
(isa? ::osx ::unix)
(isa? ::unix ::osx)

(derive ::osx ::bsd)
(defmethod home ::bsd [_] "/home")

(comment (home osx))
(prefer-method home ::unix ::bsd)
(home osx)

(defmulti  compile-cmd  (juxt :os compiler))
(defmethod compile-cmd [::osx "clang"] [m]
  (str "/usr/bin/" (compiler m)))

(defmethod compile-cmd :default [m]
  (str "Unsure where to locate " (compiler m)))

(compile-cmd osx)
(compile-cmd unix)

;; persistent BST impl with records (Chapter 6 impl uses maps.)
(defrecord TreeNode [val l r])
(assoc (TreeNode. 1 nil nil) :l (TreeNode. 2 nil nil))

(defn xconj [t v]
  (cond
    (nil? t) (TreeNode. v nil nil)
    (< v (:val t)) (TreeNode. (:val t) (xconj (:l t) v) (:r t))
    :else (TreeNode. (:val t) (:l t) (xconj (:r t) v))))

(defn xseq [t]
  (when t
    (concat (xseq (:l t)) [(:val t)] (xseq (:r t)))))

(def sample-tree (reduce xconj nil [3 5 2 4 6]))
(xseq sample-tree)
