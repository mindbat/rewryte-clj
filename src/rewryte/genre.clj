(ns rewryte.genre
  (:use rewryte.calc.bayes, rewryte.calc.stats, rewryte.db))

(def genre-features {:word-freq count-long-words
                     :sentence-length avg-sentence-length
                     :paragraph-length avg-paragraph-length-words})

(defn get-training-data
  "Given a classification type, fetch the training data for it from mongo db"
  [type]
  (or (first (search-collection {:type type} "training")) (create-document "training" (assoc initial-train-map :account_id 1 :type type))))

(defn update-genre-training-data
  "Given a new training document, update the training data in mongodb"
  [training-doc]
  (update-document "training" (update-trained-map genre-features [training-doc] (get-training-data "genre"))))