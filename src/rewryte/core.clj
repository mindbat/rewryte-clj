(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, rewryte.compare, cheshire.core)
  (:require [clojure.string :as clj-str]
            [monger.core :as mcore]))

(defn frequency-consumer [message-body]
  (let [split-body (clj-str/split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-id (second split-body)
        mongo-doc (get-document "account" doc-id)
        document (cleanup-text (mongo-doc :document))
        doc-name (mongo-doc :document_name)
        url-name (url-safe doc-name)
        frequencies (count-words document)
        freq-standard (count-words (remove-fluff document))
        max-frequency-full (apply max (vals frequencies))
        results-full (vec (sort-by val > frequencies))
        max-frequency-standard (apply max (vals freq-standard))
        results-standard (vec (sort-by val > freq-standard))
        paragraphs (convert-to-paragraphs document)
        longest-sentences (find-longest-sentences document 5)
        most-adverbs (find-most-adverbs document 5)
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-results account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard paragraphs longest-sentences most-adverbs sentence-length paragraph-length-words paragraph-length-sentences)
    (send-results-published account-id url-name)))

(defn compare-consumer [message-body]
  (let [document-id message-body
        compare-doc (get-document "compare" document-id)
        document (cleanup-text (compare-doc :document))
        frequencies (count-words document)
        results (vec (sort-by val > frequencies))
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-compare-results document-id frequencies results sentence-length paragraph-length-words paragraph-length-sentences)))

(defn paragraph-consumer [message-body]
  (let [edit-id message-body
        edit-doc (get-document "edit" edit-id)
        account-id (edit-doc :account_id)
        doc-id (edit-doc :edited_document_id)]
    (update-paragraph edit-doc)
    (queue-doc-freq account-id doc-id)
    (delete-doc "edit" edit-id)))

(def mongo-host (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1:27017/docs"))

(defn -main [consumer]
  (cond
   (= consumer "frequency") (do
                              (mcore/connect-via-uri! mongo-host)
                              (future (start-consumer "frequency.queue" frequency-consumer))
                              (future (start-consumer "compare.queue" compare-consumer))
                              (future (start-consumer "paragraph.queue" paragraph-consumer)))
   :else (println "No consumer by that name available")))
