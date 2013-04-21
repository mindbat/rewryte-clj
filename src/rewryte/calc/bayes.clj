(ns rewryte.calc.bayes)

(defn extract-features
  "Given a set of map of feature functions and labels, calculate the values for the given document"
  [feature-functions document]
  (map #(hash-map (first %) ((last %) document)) feature-functions))

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
   1) The percentage of documents that have each classification
   2) The percentage of documents in each classification that have a given value for a given feature
   3) The percentage of all documents that have a given value for a given feature
   4) The total number of documents"
  [feature-functions classified-docs]
  (->> classified-docs
       (map #(vector (:label %) (extract-features feature-functions (:text %))))
       (reduce increment-distributions initial-train-map)))