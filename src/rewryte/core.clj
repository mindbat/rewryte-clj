(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.message, rewryte.process, rewryte.db, rewryte.compare, rewryte.consumer)
  (:require [monger.core :as mcore]))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (connect-to-doc-db!)
                              (future (start-consumer "frequency.queue" frequency-consumer))
                              (future (start-consumer "compare.queue" compare-consumer))
                              (future (start-consumer "paragraph.queue" paragraph-consumer)))
   :else (println "No consumer by that name available")))
