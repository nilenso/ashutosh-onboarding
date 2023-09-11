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

(defprotocol FIXO
  (fixo-push [fixo value])
  (fixo-pop [fixo])
  (fixo-peek [fixo]))

(extend-type TreeNode
  FIXO
  (fixo-push [node val] (xconj node val)))

(extend-type clojure.lang.PersistentVector
  FIXO
  (fixo-push [vector* val] (conj vector* val)))

(fixo-push sample-tree 10)
(fixo-push [1] 10)
(comment (fixo-peek sample-tree))

(extend-type TreeNode
  FIXO
  (fixo-peek [node]
    (if-let [l (get node :l)]
      (recur l)
      (get node :val))))

(fixo-peek sample-tree)
;; the following throws an error because a protocol must be fully extended to
;; have an effect.
(comment (fixo-push sample-tree 20))


;; using mix-ins
(def fixo-push-mixin {:fixo-push (fn [vector* val] (conj vector* val))})
(def fixo-peek-mixin {:fixo-peek (fn [vector*] (peek vector*))})
(def fixo-pop-mixin {:fixo-pop (fn [vector*] (pop vector*))})
(extend clojure.lang.PersistentVector FIXO (merge fixo-push-mixin fixo-peek-mixin fixo-pop-mixin))
(fixo-push [1 2 3] 4)
(fixo-peek [1 2 3 4])
(fixo-pop [1 2 3 4])

(defn fixed-fixo
  ([limit] (fixed-fixo limit []))
  ([limit vector]
   (reify FIXO
     (fixo-push [this value]
       (if (< (count vector) limit)
         (fixed-fixo limit (conj vector value))
         this))
     (fixo-peek [_]
       (peek vector))
     (fixo-pop [_]
       (pop vector)))))

(fixo-peek (fixo-push (fixed-fixo 0) 10))

;; inline protocol implementation with defrecord.
(defrecord AnotherTreeNode [val l r] FIXO
           (fixo-push [_ v] (if (< v val)
                              (AnotherTreeNode. val (fixo-push l v) r)
                              (AnotherTreeNode. val l (fixo-push r v))))
           (fixo-peek [_] (if l (fixo-peek l) val)) ; recur won't work
           (fixo-pop [_] (if l (AnotherTreeNode. val (fixo-pop l) r) r)))

(extend-type nil FIXO
             (fixo-push [_ v] (AnotherTreeNode. v nil nil))
             (fixo-peek [_] nil)
             (fixo-pop [_] nil))

(def sample-tree2 (reduce fixo-push (AnotherTreeNode. 3 nil nil) [5 2 4 6]))
(xseq sample-tree2)

;; records are maps (already implement ISeq) so the following fails.
(comment (defrecord InfiniteConstant [i]
           clojure.lang.ISeq
           (seq [this]
             (lazy-seq (cons i (seq this))))))

;; following works!
(deftype InfiniteConstant [i]
  clojure.lang.ISeq
  (seq [this]
    (lazy-seq (cons i (seq this)))))

(take 3 (InfiniteConstant. 5))

;; And it doesn't implement keyword look ups and other map things.
(:i (InfiniteConstant. 5))

(deftype FinalTreeNode [val l r]
  FIXO
  (fixo-push [_ v] (if (< v val)
                     (FinalTreeNode. val (fixo-push l v) r)
                     (FinalTreeNode. val l (fixo-push r v))))
  (fixo-peek [_] (if l (fixo-peek l) val))
  (fixo-pop [_] (if l
                  (FinalTreeNode. val (fixo-pop l) r)
                  r))

  clojure.lang.IPersistentStack
  (cons [this v] (fixo-push this v))
  (peek [this] (fixo-peek this))
  (pop [this] (fixo-pop this))

  clojure.lang.Seqable
  (seq [_] (concat (seq l) [val] (seq r))))

(extend-type nil
  FIXO
  (fixo-push [_ v]
    (FinalTreeNode. v nil nil)))

(seq (into (FinalTreeNode. 3 nil nil) [5 2 4 6]))
