(ns rewryte.edits
  (:use rewryte.process))

(defn add-paragraphs
  "Add a version of the document that's been converted to paragraphs"
  [doc-map]
  (assoc doc-map :paragraphs (convert-to-paragraphs (:document doc-map))))

(defn add-longest-sentences
  "Find and add the longest sentences to the document map"
  [doc-map]
  (assoc doc-map :longest_sentences (find-longest-sentences (:document doc-map) 5)))

(defn add-most-adverbs
  "Find the sentences with the most adverbs and add them to the document map"
  [doc-map]
  (assoc doc-map :most_adverbs (find-most-adverbs (:document doc-map) 5)))

(defn calculate-edits
  "Find all the suggested edits for the given document"
  [doc-map]
  (-> doc-map
      add-paragraphs
      add-longest-sentences
      add-most-adverbs))