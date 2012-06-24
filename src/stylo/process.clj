(ns stylo.process
  (:use clojure.string))

(defn convert-line-to-words
  "Convert a line into a sequence of words"
  [line]
  (split (replace (lower-case line) #"[^ a-zA-Z0-9]+" "") #"\s+"))

(defn count-words
  "Count the number of words in a single line"
  [line]
  (frequencies (convert-line-to-words line)))
