(ns rewryte.utils
  (:use clojure.java.io, rewryte.db, rewryte.message))

(defn find-text-files
  "Given a directory name, find all the text files in the directory"
  [dir-name]
  (map #(str dir-name "/" (.getName %)) (rest (file-seq (file dir-name)))))

(defn convert-file-to-classified-doc
  "Build a classified doc data structure from a given text file"
  [label text-file]
  {:label label :text (slurp text-file)})

(defn save-training-document
  "Save a training doc to mongodb"
  [classifier-name training-doc]
  (create-document classifier-name training-doc))

(defn queue-training-doc
  "Put a document into the genre training queue"
  [document]
  (queue-doc "genre_train.queue" 1 (document :_id)))

(defn upload-genre-docs
  "Given a directory and a genre, upload all the documents in that directory to the genre training queue"
  [directory genre]
  (connect-to-doc-db!)
  (dorun (map queue-training-doc (map #(save-training-document "genre" %) (map #(convert-file-to-classified-doc genre %) (find-text-files directory))))))