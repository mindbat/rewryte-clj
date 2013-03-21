(ns rewryte.stats)

(defn clean-document
  "Cleanup the special characters in the document"
  [doc-map]
  (assoc doc-map :document (cleanup-text (:document doc-map))))

(defn add-word-freq
  "Add word frequencies to the document map"
  [doc-map]
  (assoc doc-map :frequencies (count-words (:document doc-map))))

(defn add-full-results
  "Add max word frequency and sorted word frequencies to the doc map"
  [doc-map]
  (let [freq (:frequencies doc-map)
        max-freq (apply max (vals freq))
        sorted-freq (vec (sort-by val > freq))]
    (assoc doc-map :results_full {:max_frequency max-freq :results sorted-freq})))

(defn add-standard-results
  "Add max word frequency and sorted word frequencies to the doc map, with common words filtered out"
  [doc-map]
  (let [freq (count-words (remove-fluff (:document doc-map)))
        max-freq (apply max (vals freq))
        sorted-freq (vec (sort-by val > freq))]
    (assoc doc-map :results_standard {:max_frequency max-freq :results sorted-freq})))

(defn add-avg-sentence-length
  "Add the average sentence length in words to the doc map"
  [doc-map]
  (assoc doc-map :sentence_length (avg-sentence-length (:document doc-map))))

(defn add-para-length-words
  "Add the average paragraph length (in words) to the doc map"
  [doc-map]
  (assoc doc-map :paragraph_length_words (avg-paragraph-length-words (:document doc-map))))

(defn add-para-length-sentences
  "Add the average paragraph length (in sentences) to the doc map"
  [doc-map]
  (assoc doc-map :paragraph_length_sentences (avg-paragraph-length-sentences (:document doc-map))))

(defn calculate-stats
  "Calculate the statistics (word-frequency, etc) for the given document"
  [document]
  (-> document
      clean-document
      add-word-freq
      add-full-results
      add-standard-results
      add-avg-sentence-length
      add-para-length-words
      add-para-length-sentences))