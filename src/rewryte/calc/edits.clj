(ns rewryte.calc.edits
  (:use rewryte.calc.core))

(defn sort-by-word-length
  "Sort the given sequence of strings according to the number of words in each string"
  [string-seq]
  (sort-by #(count (convert-to-words (first %))) string-seq))

(defn sort-by-adverb-number
  "Sort a set of paragraphs by the number of adverbs they have"
  [para-seq]
  (sort-by #(count (last %)) para-seq))

(defn find-longest-sentences
  "Find the X longest sentences in the document"
  [text num-sentences]
  (let [paragraphs (convert-to-paragraphs text)
        sentences-by-paragraph (map convert-to-sentences paragraphs)
        indexed-sentences (partition 2 (flatten (map-indexed (fn [index item] (map #(vector % index) item)) sentences-by-paragraph)))
        sorted-sentences (sort-by-word-length indexed-sentences)]
    (reverse (take-last num-sentences sorted-sentences))))

(defn find-most-adverbs
  "Find the paragraphs with the most adverbs"
  [text num-paragraphs]
  (let [paragraphs (convert-to-paragraphs text)
        indexed-paragraphs (map-indexed (fn [index item] (vector index (find-adverbs item))) paragraphs)
        sorted-paragraphs (sort-by-adverb-number indexed-paragraphs)]
    (reverse (take-last num-paragraphs sorted-paragraphs))))

(defn add-paragraphs
  "Add a version of the document that's been converted to paragraphs"
  [doc-map]
  (assoc doc-map :paragraphs (future (convert-to-paragraphs (:document doc-map)))))

(defn add-longest-sentences
  "Find and add the longest sentences to the document map"
  [doc-map]
  (assoc doc-map :longest_sentences (future (find-longest-sentences (:document doc-map) 5))))

(defn add-most-adverbs
  "Find the sentences with the most adverbs and add them to the document map"
  [doc-map]
  (assoc doc-map :most_adverbs (future (find-most-adverbs (:document doc-map) 5))))

(defn calculate-edits
  "Find all the suggested edits for the given document"
  [doc-map]
  (-> doc-map
      add-paragraphs
      add-longest-sentences
      add-most-adverbs))