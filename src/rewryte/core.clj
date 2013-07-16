(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.message, rewryte.db, rewryte.consumer))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (connect-to-doc-db!)
                              (future (start-consumer "frequency.queue" frequency-consumer))
                              (future (start-consumer "genre_train.queue" genre-train-consumer))
                              (future (start-consumer "paragraph.queue" paragraph-consumer)))
   :else (println "No consumer by that name available")))
