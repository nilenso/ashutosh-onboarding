(ns fhir-data-analysis.utils)

(defn- get-or-get-in [m k-or-ks]
  (if (seqable? k-or-ks)
    (get-in m k-or-ks)
    (get m k-or-ks)))

(defn transform
  "Transforms data in the input map `m` using the given transformations map
   `tfs`.

       (transform {:a {:b [1 2 3 4 5]}}
                  {:nums [:a :b] ; same [[:a :b] identity]
                   :sum [[:a :b] (partial apply +)]})
       ;=> {:nums [1 2 3 4 5], :sum 15}
   "
  [m tfs]
  (reduce
   (fn reducer [transformed [k v]]
     (cond
       (map? v) (assoc transformed k (reduce reducer (get transformed k) v))
       (and (seqable? v) (= 2 (count v)) (fn? (last v)))
       (assoc transformed k ((last v) (get-or-get-in m (first v))))
       :else (assoc transformed k (get-or-get-in m v))))
   {}
   tfs))

(defn transformer
  "Returns a closure for [[transform]] on the input transformations map `tfs`."
  [tfs]
  (fn [m] (transform m tfs)))
