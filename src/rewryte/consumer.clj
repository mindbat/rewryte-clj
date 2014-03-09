(ns rewryte.consumer
  (:require [clojure.string :as str]
            [rewryte.db :refer [save-recommendations]]
            [rewryte.calc :refer [calculate-recommendations]]
            [rewryte.message :refer [publish-results]]
            [rewryte.s3 :refer [fetch-s3-document]]
            [rewryte.tika :refer [extract-text]]))

(defn url-safe
  "Generate a url-safe version of the string"
  [text]
  (str/replace (str/lower-case text) #"[^0-9a-zA-Z]" "_"))

(defn add-url-name
  "Add the url name for the document"
  [doc-map]
  (assoc doc-map :url_name (url-safe (:document_name doc-map))))

(defn realize-map
  "Given a hash-map with unrealized values, realize all values"
  [future-map]
  (reduce #(assoc %1 (first %2) @(second %2))
          future-map
          (filter #(future? (second %)) future-map)))

(defn recommend-consumer
  [message-body]
  (let [[bucket s3-id report-id] (str/split message-body #":")
        account-id (first (str/split s3-id #"-"))]
    (->> (fetch-s3-document bucket s3-id)
         extract-text
         calculate-recommendations
         add-url-name
         realize-map
         save-recommendations
         publish-results)))


#_(defn extract-consumer [message-body]
  (let [[bucket s3-id] (str/split message-body #":")
        account-id (first (str/split s3-id #"-"))]
    (->> (fetch-s3-document bucket s3-id)
         extract-text
         (save-doc-text (Integer/parseInt account-id) s3-id)
         (queue-doc "frequency.queue" account-id))))
