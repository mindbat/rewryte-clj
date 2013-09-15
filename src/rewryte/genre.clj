(ns rewryte.genre
  (:use rewryte.calc.bayes, rewryte.calc.stats, rewryte.db))

(def genre-features {:word-freq count-long-words
                     :sentence-length avg-sentence-length
                     :paragraph-length avg-paragraph-length-words})

(def training-keys [:label-dist :feature-value-dist :feature-label-dist :total-docs])

(defn prep-training-save
  "Convert training data keys back to strings before saving"
  [training-map]
  (apply assoc training-map (flatten (map #(vector % (str (training-map %))) training-keys))))

(defn prep-training-use
  "Convert training data keys from strings back to maps before use"
  [training-map]
  (apply assoc training-map (flatten (map #(vector % (read-string (training-map %))) training-keys))))

(defn get-training-data
  "Given a classification type, fetch the training data for it from mongo db. Convert training data from strings to maps before returning"
  [type]
  (prep-training-use
   (or (first (search-collection {:type type} "training"))
       (create-document "training" (prep-training-save (assoc initial-train-map :account_id 1 :type type))))))

(defn update-genre-training-data
  "Given a new training document, update the training data in mongodb"
  [training-doc]
  (->> (get-training-data "genre")
       (update-trained-map genre-features [training-doc])
       (prep-training-save)
       (update-document "training")))
