(ns stylo.process
  (:use clojure.string))

(defn convert-to-words
  "Convert a line into a sequence of words"
  [line]
  (split (replace (lower-case line) #"[^ a-zA-Z0-9]+" "") #"\s+"))

(defn count-words
  "Count the number of words in a string"
  [incoming]
  (frequencies (convert-to-words incoming)))

(defn scramble-words
  "Scramble up the words in a string"
  [incoming]
  (shuffle (convert-to-words incoming)))
