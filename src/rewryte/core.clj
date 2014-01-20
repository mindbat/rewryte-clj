(ns rewryte.core
  (:gen-class :main true)
  (:require [rewryte.consumer :refer [recommend-consumer]]
            [rewryte.db :refer [connect-to-doc-db!]]
            [rewryte.message :refer [start-consumer]]))

(defn -main [consumer]
  (cond
   (= consumer "recommend") (do
                              (connect-to-doc-db!)
                              (start-consumer "recommend.queue"
                                              recommend-consumer))
   :else (println "No consumer by that name available")))
