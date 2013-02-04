(ns rewryte.process
  (:require [clojure.string :as clj-str]))

(defn url-safe
  "Generate a url-safe version of the string"
  [text]
  (clj-str/replace (clj-str/lower-case text) #"[^0-9a-zA-Z]" "_"))

(defn split-n
  "Split the given sequence into a vector of vectors made of every nth element of the original sequence.
   Ex:
      (split-n 2 [1 3 5 7 9 11 13 15 17 19 21])
    => [[1 3] [5 7] [9 11] [13 15] [17 19] [21]]"
  [n split-seq]
  (loop [take-num n
         rtn-seq []
         process-seq split-seq]
    (if (< (count process-seq) take-num)
      (conj rtn-seq (vec process-seq))
      (recur take-num (conj rtn-seq (vec (take n process-seq))) (drop n process-seq)))))

(defn convert-to-words
  "Convert a line into a sequence of words"
  [line]
  (clj-str/split (clj-str/replace (clj-str/lower-case line) #"[^ a-zA-Z0-9\n\r]+" "") #"\s+"))

(defn convert-to-keywords
  "Convert a line into a sequence of keywords"
  [line]
  (map keyword (convert-to-words line)))

(defn count-words
  "Count the number of words in a string"
  [incoming]
  (frequencies (convert-to-keywords incoming)))

(defn cleanup-text
  "Prepare text for processing"
  [text]
  (clj-str/replace text #"\r" ""))

(defn convert-to-paragraphs
  "Convert a text into a sequence of paragraphs"
  [text]
  (filter #(> (count %) 0) (clj-str/split text #"\n\n+")))

(defn convert-to-sentences
  "Convert the incoming text into a sequence of sentences"
  [text]
  (map first (re-seq #"(\")?(\w+[-/<>*&%$#@()+=\[\]{}~ ;,':\n]+)+(\w+[.?!])(\")?" text)))

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

(defn sort-by-word-length
  "Sort the given sequence of strings according to the number of words in each string"
  [string-seq]
  (sort-by #(count (convert-to-words (first %))) string-seq))

(defn find-longest-sentences
  "Find the X longest sentences in the document"
  [text num-sentences]
  (let [paragraphs (convert-to-paragraphs text)
        sentences-by-paragraph (map convert-to-sentences paragraphs)
        indexed-sentences (partition 2 (flatten (map-indexed (fn [index item] (map #(vector % index) item)) sentences-by-paragraph)))
        sorted-sentences (sort-by-word-length indexed-sentences)]
    (take-last num-sentences sorted-sentences)))

(defn adverb?
  "Return true if the word given is an adverb"
  [word]
  (not (nil? (re-matches #"[a-z]+ly$" word))))

(defn find-adverbs
  "Find all the adverbs in the given text"
  [text]
  (let [words (convert-to-words text)]
    (set (filter adverb? words))))

(defn sort-by-adverb-number
  "Sort a set of paragraphs by the number of adverbs they have"
  [para-seq]
  (sort-by #(count (last %)) para-seq))

(defn find-most-adverbs
  "Find the paragraphs with the most adverbs"
  [text num-paragraphs]
  (let [paragraphs (convert-to-paragraphs text)
        indexed-paragraphs (map-indexed (fn [index item] (vector index (find-adverbs item))) paragraphs)
        sorted-paragraphs (sort-by-adverb-number indexed-paragraphs)]
    (take-last num-paragraphs sorted-paragraphs)))

(defn remove-articles
  "Remove the articles from a given text"
  [text]
  (let [articles #"\s(the|a|an)\s"]
    (clj-str/replace text articles " ")))

(defn remove-prepositions
  "Remove the prepositions from a given text"
  [text]
  (let [articles #"\s(of|on|in|to|from|with|for|by)\s"]
    (clj-str/replace text articles " ")))

(defn remove-pronouns
  "Remove the pronouns from a given text"
  [text]
  (let [articles #"\s(he|she|it|i|you|your|we|our|my|them|they|us|his|her|him|hers|this|that|these|those|their|theirs)\s"]
    (clj-str/replace text articles " ")))

(defn remove-conjunctions
  "Remove the conjunctions from a given text"
  [text]
  (let [articles #"\s(and|but|or)\s"]
    (clj-str/replace text articles " ")))

(defn remove-fluff
  "Remove the small, common words from the text"
  [text]
  (-> text
    (clj-str/lower-case)
    (remove-articles)
    (remove-prepositions)
    (remove-pronouns)
    (remove-conjunctions)))
