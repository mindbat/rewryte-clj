(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.message, rewryte.db, rewryte.consumer))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (connect-to-doc-db!)
                              (start-consumer "frequency.queue" frequency-consumer))
   (= consumer "compare") (do
                            (connect-to-doc-db!)
                            (start-consumer "compare.queue" compare-consumer))
   (= consumer "paragraph") (do
                              (connect-to-doc-db!)
                              (start-consumer "paragraph.queue" paragraph-consumer))
   :else (println "No consumer by that name available")))
