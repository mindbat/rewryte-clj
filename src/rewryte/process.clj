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

(defn paginate
  "Split a string up into a vector of text pages"
  [text]
  (map #(apply str %) (split-n 250 (convert-to-words text))))

(defn avg-sentence-length
  "Calculate the average sentence length in words for the given text"
  [text]
  (let [sentences (split text #"\. ")
        num-sentences (count sentences)
        total-words (count (convert-to-words text))]
  (float (/ total-words num-sentences))))
