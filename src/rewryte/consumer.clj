(ns rewryte.consumer
  (:require [clojure.string :as str]
            [rewryte.db :refer [save-recommendations]]
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
  (reduce #(assoc %1 (first %2) @(second %2)) future-map (filter #(future? (second %)) future-map)))

(defn recommend-consumer
  [message-body]
  (->> message-body
       fetch-s3-document
       extract-text
       calculate-recommendations
       add-url-name
       realize-map
       save-recommendations
       publish-results))
