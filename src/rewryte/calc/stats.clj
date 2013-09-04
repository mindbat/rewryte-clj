(ns rewryte.calc.stats
  (:use rewryte.calc.core))

(defn clean-document
  "Cleanup the special characters in the document"
  [doc-map]
  (assoc doc-map :document (cleanup-text (:document doc-map))))

(defn count-words
  "Count the number of words in a string"
  [incoming]
  (frequencies (convert-to-keywords incoming)))

(defn count-long-words
  "Count the number of long words in a string"
  [incoming]
  (frequencies (map keyword (filter long? (convert-to-words incoming)))))

(defn avg-sentence-length
  "Calculate the average sentence length in words for the given text"
  [text]
  (let [sentences (convert-to-sentences text)
        num-sentences (count sentences)
        total-words (count (convert-to-words text))]
    (float (/ total-words (max 1 num-sentences)))))

(defn avg-paragraph-length-words
  "Calculate the average paragraph length in words for the given text"
  [text]
  (let [paragraphs (convert-to-paragraphs text)
        num-paragraphs (count paragraphs)
        total-words (count (convert-to-words text))]
    (float (/ total-words (max 1 num-paragraphs)))))

(defn avg-paragraph-length-sentences
  "Calculate the average paragraph length in sentences for the given text"
  [text]
  (let [paragraphs (convert-to-paragraphs text)
        num-paragraphs (count paragraphs)
        total-sentences (count (convert-to-sentences text))]
    (float (/ total-sentences (max 1 num-paragraphs)))))

(defn add-word-freq
  "Add word frequencies to the document map"
  [doc-map]
  (assoc doc-map :frequencies (future (count-words (:document doc-map)))))

(defn add-long-word-freq
  "Add histogram of words longer than 7 characters to the document map"
  [doc-map]
  (assoc doc-map :long_frequencies (frequencies (map keyword (filter long? (convert-to-words (:document doc-map)))))))

(defn add-full-results
  "Add max word frequency and sorted word frequencies to the doc map"
  [doc-map]
  (let [freq @(:frequencies doc-map)
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
  (assoc doc-map :sentence_length (future (avg-sentence-length (:document doc-map)))))

(defn add-para-length-words
  "Add the average paragraph length (in words) to the doc map"
  [doc-map]
  (assoc doc-map :paragraph_length_words (future (avg-paragraph-length-words (:document doc-map)))))

(defn add-para-length-sentences
  "Add the average paragraph length (in sentences) to the doc map"
  [doc-map]
  (assoc doc-map :paragraph_length_sentences (future (avg-paragraph-length-sentences (:document doc-map)))))

(defn calculate-stats
  "Calculate the statistics (word-frequency, etc) for the given document"
  [document]
  (-> document
      clean-document
      add-word-freq
      add-long-word-freq
      add-full-results
      add-standard-results
      add-avg-sentence-length
      add-para-length-words
      add-para-length-sentences))

(defn calculate-genre-stats
  "Calculate the stats we need to assign a genre to this document"
  [document]
  (-> document
      clean-document
      add-long-word-freq
      add-avg-sentence-length
      add-para-length-words))
