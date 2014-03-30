(ns rewryte.core
  (:gen-class :main true)
  (:require [rewryte.consumer :refer [recommend-consumer]]
            [rewryte.message :refer [start-consumer]]))

(defn -main [consumer]
  (cond
   (= consumer "recommend") (start-consumer "recommend.queue"
                                            recommend-consumer)
   :else (println "No consumer by that name available")))
