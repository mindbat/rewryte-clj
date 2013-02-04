(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, rewryte.compare, cheshire.core)
  (:require [clojure.string :as clj-str]))

(defn frequency-consumer [message-body]
  (let [split-body (clj-str/split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-id (second split-body)
        mongo-doc (get-document account-id doc-id)
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
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-results account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard paragraphs longest-sentences sentence-length paragraph-length-words paragraph-length-sentences)
    (queue-doc-compare account-id doc-id)))

(defn compare-consumer [message-body]
  (let [split-body (clj-str/split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-id (second split-body)
        mongo-doc (get-document account-id doc-id)
        url-name (mongo-doc :url_name)
        score (compute-score mongo-doc "perfect.queue")]
    (save-score account-id doc-id score)
    (send-results-published account-id url-name)))

(defn paragraph-consumer [message-body]
  (let [edit-id message-body
        edit-doc (get-edit-doc edit-id)
        account-id (edit-doc :account_id)
        doc-id (edit-doc :edited_document_id)]
    (update-paragraph edit-doc)
    (queue-doc-freq account-id doc-id)
    (delete-doc "edit" edit-id)))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (do
                                (future (start-consumer "frequency.queue" frequency-consumer))
                                (future (start-consumer "compare.queue" compare-consumer))
                                (future (start-consumer "paragraph.queue" paragraph-consumer)))
    :else (println "No consumer by that name available")))
