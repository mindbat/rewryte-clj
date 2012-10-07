(ns rewryte.compare
  (:use rewryte.mongo, clojure.string))

(defn length-range
  "Compute the max and min values allowed for the given length"
  [length]
  (let [variance (Math/ceil (* length 0.25))
        lowest (- length variance)
        highest (+ length variance)]
    {:min lowest :max highest}))

(defn compare-query
  "Get the comparison query for the given document"
  [document]
  (let [sentence-length (:sentence_length document)
        sentence-range (length-range sentence-length)
        paragraph-length-words (:paragraph_length_words document)
        para-word-range (length-range paragraph-length-words)
        paragraph-length-sentences (:paragraph_length_sentences document)
        para-sentence-range (length-range paragraph-length-sentences)]
    {:sentence_length {"$gte" (:min sentence-range) "$lte" (:max sentence-range)}
     :paragraph_length_words {"$gte" (:min para-word-range) "$lte" (:max para-word-range)}
     :paragraph_length_sentences {"$gte" (:min para-sentence-range) "$lte" (:max para-sentence-range)}}))

(defn compute-score
  "Compute the comparison query for the given document"
  [document compare-collection]
  (let [query (compare-query document)]
    (count (search-collection query "perfect"))))
