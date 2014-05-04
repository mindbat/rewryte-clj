(ns rewryte.calc
  (:require [rewryte.db :refer [get-cliches]]))

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

(defn find-cliche-matches
  [cliches text]
  (let [match-seq (mapcat #([(:id %) (re-seq (:expression %) text)])
                          cliches)]
    (for [[id match] match-seq]
      (if (coll? match)
        [id (first match)]
        [id match]))))

(defn calculate-recommendations
  [doc-map]
  (let [cliches (get-cliches)
        text (:text doc-map)]
    (assoc doc-map :cliches
           (mapcat (partial find-offsets text)
                   (find-cliche-matches cliches text)))))
