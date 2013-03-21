(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, rewryte.compare, rewryte.consumer)
  (:require [monger.core :as mcore]))

(def mongo-host (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1:27017/docs"))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (mcore/connect-via-uri! mongo-host)
                              (future (start-consumer "frequency.queue" frequency-consumer))
                              (future (start-consumer "compare.queue" compare-consumer))
                              (future (start-consumer "paragraph.queue" paragraph-consumer)))
   :else (println "No consumer by that name available")))
