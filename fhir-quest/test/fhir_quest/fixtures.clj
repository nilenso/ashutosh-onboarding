(ns fhir-quest.fixtures
  (:require [clojure.java.io :as io]))

(defn with-tmp-dir [f]
  (let [tmp-dir (-> "java.io.tmpdir"
                    (System/getProperty)
                    (io/file (str (System/currentTimeMillis))))]
    (.mkdirs tmp-dir)
    (f tmp-dir)
    (run! io/delete-file (reverse (file-seq tmp-dir)))))

(defn with-tmp-file [file-name f]
  (with-tmp-dir #(f (-> %
                        (io/file file-name)))))
