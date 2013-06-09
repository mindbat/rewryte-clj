(ns rewryte.calc.bayes
  (:use clojure.set))

(defn extract-features
  "Given a map of feature functions and labels, calculate the values for the given document"
  [feature-functions document]
  (reduce merge {} (map #(hash-map (first %) ((last %) document)) feature-functions)))

(def initial-train-map {:label-dist {} :feature-value-dist {} :feature-label-dist {} :total-docs 0})

(defn inc-map-value
  "Given a map and a key, increment the key's value by one"
  [inc-map key]
  (assoc inc-map key (+ 1 (get inc-map key 0))))

(defn increment-feature-value-dist
  "Given a current feature-value-distribution, update the map based on the values in the new hash-map of features and values"
  [current-dist new-feature-map]
  (reduce #(assoc %1 (first %2) (inc-map-value (get %1 (first %2) {}) (second %2))) current-dist new-feature-map))

(defn increment-distributions
  "Given a distribution map and a vector of the form [label feature-map], increment the distributions of features, values and labels"
  [{:keys [label-dist feature-value-dist feature-label-dist] :as dist-map} [label features :as feature-vec]]
  (-> dist-map
      (inc-map-value :total-docs)
      (assoc :label-dist (inc-map-value label-dist label))
      (assoc :feature-value-dist (increment-feature-value-dist feature-value-dist features))
      (assoc :feature-label-dist (assoc feature-label-dist label (increment-feature-value-dist (get feature-label-dist label {}) features)))))

(defn train
  "Given a set of feature functions and pre-classfied documents, calculate:
   1) The number of documents that have each classification (:label-dist)
   2) The number of documents in each classification that have a given value for a given feature (:feature-value-dist)
   3) The number of all documents that have a given value for a given feature (:feature-label-dist)
   4) The total number of documents (:total-docs)"
  [feature-functions classified-docs]
  (->> classified-docs
       (map #(vector (:label %) (extract-features feature-functions (:text %))))
       (reduce increment-distributions initial-train-map)))

(defn get-max-key
  "Given a map of keys and numerical values, get the key with the maximum value"
  [max-map]
  (last (last (apply sorted-map (flatten (vec (map-invert max-map)))))))

(defn p-feature-category
  "Given a feature, value and category, find the probability that a document in the category has the given feature-value.
   We default the probability to 1 instead of 0, so if the feature-value is not found for the category, we ignore it"
  [[feature value] category trained-map]
  (get-in trained-map [:feature-label-dist category feature value] 1))

(defn p-category
  "Given a category, find the probability the given document is in that category"
  [doc-features category trained-map]
  (let [total-prob-cat (/ ((trained-map :label-dist) category) (trained-map :total-docs))]
    (reduce * (map #(p-feature-category % category trained-map) doc-features))))

(defn classify
  "Given a document, a set of feature functions and a map of feature distributions, calculate the most likely category for a document"
  [document feature-functions trained-map]
  (let [doc-features (extract-features feature-functions document)
        categories (keys (trained-map :label-dist))]
    (get-max-key (zipmap categories (map #(p-category doc-features % trained-map) categories)))))

(defn classifier
  "Given a set of feature functions and classified documents, create a function that can classify new documents"
  [feature-functions classified-docs]
  (fn [document] (classify document feature-functions (train feature-functions classified-docs))))