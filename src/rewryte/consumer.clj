(ns rewryte.consumer
  (:require [clojure.string :as str]
            [rewryte.db :refer [save-recommendations]]
            [rewryte.calc :refer [calculate-recommendations]]
            [rewryte.message :refer [publish-results]]
            [rewryte.s3 :refer [fetch-s3-document
                                save-plain-text-doc]]
            [rewryte.tika :refer [extract-text]]))

(defn realize-map
  "Given a hash-map with unrealized values, realize all values"
  [future-map]
  (reduce #(assoc %1 (first %2) @(second %2))
          future-map
          (filter #(future? (second %)) future-map)))

(defn recommend-consumer
  [message-body]
  (let [[bucket s3-id report-id] (str/split message-body #":")
        account-id (first (str/split s3-id #"-"))
        plain-bucket (if (.contains bucket "dev")
                       "rewryte-plain-dev"
                       "rewryte-plain")]
    (->> (fetch-s3-document bucket s3-id)
         extract-text
         (save-plain-text-doc plain-bucket)
         calculate-recommendations
         (save-recommendations report-id)
         (publish-results account-id))))
