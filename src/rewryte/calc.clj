(ns rewryte.calc
  (:require [rewryte.db :refer [get-cliches]]
            [clojure.tools.logging :as log]))

(defn find-offsets
  "Find the indexes at which the given sub string occurs in the larger string"
  [search-string [id sub-string]]
  (let [sub-length (.length sub-string)]
    (loop [idx 0
           offsets []]
      (let [offset (.indexOf search-string sub-string idx)]
        (if (= offset -1)
          offsets
          (recur (+ offset sub-length)
                 (conj offsets [id offset sub-length])))))))

(defn fix-match-seq
  [expression text]
  (map #(if (string? %)
          %
          (first %))
       (re-seq expression text)))

(defn find-cliche-matches
  [cliches text]
  (reduce (fn [coll val]
            (let [id (:id val)
                  match-seq (fix-match-seq (:expression val) text)]
              (if (seq match-seq)
                (apply conj coll (for [match match-seq]
                                   [id match]))
                coll)))
             []
             cliches))

(defn calculate-recommendations
  [doc-map]
  (log/info "calculating recommendations")
  (let [cliches (get-cliches)
        text (:text doc-map)]
    (assoc doc-map :cliches
           (doall (mapcat (partial find-offsets text)
                          (find-cliche-matches cliches text))))))
