(ns rewryte.process
  (:use clojure.string))

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
  (split (replace (lower-case line) #"[^ a-zA-Z0-9]+" "") #"\s+"))

(defn convert-to-keywords
  "Convert a line into a sequence of keywords"
  [line]
  (map keyword (convert-to-words line)))

(defn count-words
  "Count the number of words in a string"
  [incoming]
  (frequencies (convert-to-keywords incoming)))

(defn convert-to-paragraphs
  "Convert a text into a sequence of paragraphs"
  [text]
  (map #(replace % #"[\n\r]" "") (split text #"(\n\n|\r\n\r)")))

(defn paginate
  "Split a string up into a vector of text pages"
  [text]
  (split-n 10 (convert-to-paragraphs text)))

(defn convert-to-sentences
  "Convert the incoming text into a sequence of sentences"
  [text]
  (split text #"[.?!]\s+"))

(defn avg-sentence-length
  "Calculate the average sentence length in words for the given text"
  [text]
  (let [sentences (convert-to-sentences text)
        num-sentences (count sentences)
        total-words (count (convert-to-words text))]
  (float (/ total-words num-sentences))))

(defn avg-paragraph-length-words
  "Calculate the average paragraph length in words for the given text"
  [text]
  (let [paragraphs (convert-to-paragraphs text)
        num-paragraphs (count paragraphs)
        total-words (count (convert-to-words text))]
  (float (/ total-words num-paragraphs))))

(defn avg-paragraph-length-sentences
  "Calculate the average paragraph length in sentences for the given text"
  [text]
  (let [paragraphs (convert-to-paragraphs text)
        num-paragraphs (count paragraphs)
        total-sentences (count (convert-to-sentences text))]
  (float (/ total-sentences num-paragraphs))))
