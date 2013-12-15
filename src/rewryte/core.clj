(ns rewryte.core
  (:gen-class :main true)
  (:require [rewryte.consumer :refer [extract-consumer
                                      frequency-consumer
                                      genre-train-consumer
                                      paragraph-consumer]]
            [rewryte.db :refer [connect-to-doc-db!]]
            [rewryte.message :refer [start-consumer]]))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (connect-to-doc-db!)
                              (start-consumer "frequency.queue" frequency-consumer))
   (= consumer "genre") (do
                            (connect-to-doc-db!)
                            (start-consumer "genre_train.queue" genre-train-consumer))
   (= consumer "paragraph") (do
                              (connect-to-doc-db!)
                              (start-consumer "paragraph.queue" paragraph-consumer))
   (= consumer "extract") (do
                              (connect-to-doc-db!)
                              (start-consumer "extract.queue" extract-consumer))
   :else (println "No consumer by that name available")))
