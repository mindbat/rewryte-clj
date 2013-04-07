(ns rewryte.calc.core
  (:use opennlp.nlp, opennlp.tools.filters)
  (:require [clojure.string :as clj-str]))

(pos-filter adverbs #"^RB")

(def pos-tag (make-pos-tagger "models/en-pos-maxent.bin"))

(def tokenize (make-tokenizer "models/en-token.bin"))

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

(defn cleanup-text
  "Prepare text for processing"
  [text]
  (clj-str/replace text #"\r" ""))

(defn convert-to-paragraphs
  "Convert a text into a sequence of paragraphs"
  [text]
  (filter #(> (count %) 0) (clj-str/split text #"\n\n+")))

(def convert-to-sentences (make-sentence-detector "models/en-sent.bin"))

(defn adverb?
  "Return true if the word given is an adverb"
  [word]
  (not (nil? (re-matches #"[a-z]+ly$" word))))

(defn find-adverbs
  "Find all the adverbs in the given text"
  [text]
  (->> text
       tokenize
       pos-tag
       adverbs
       (map first)
       vec))

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
