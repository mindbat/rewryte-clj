(ns rewryte.consumer
  (:use rewryte.process, rewryte.mongo, rewryte.rabbit)
  (:require [clojure.string :as clj-str]))

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

(defn parse-message
  "Parse an incoming message from rabbit-mq"
  [message-body]
  (let [split-body (clj-str/split message-body #":")]
    {:account_id (first split-body) :doc_id (second split-body)}))

(defn add-url-name
  "Add the url name for the document"
  [doc-map]
  (assoc doc-map :url_name (url-safe (:document_name doc-map))))

(comment
  (defn frequency-consumer
    "Process incoming messages from the frequency queue"
    [message-body]
    (-> message-body
        parse-message
        (get-document "account")
        calculate-stats
        calculate-edits
        add-url-name
        (save-document "account")
        publish-results)))

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

(comment
  (defn compare-consumer
    "Process incoming messages from the compare queue"
    [message-body]
    (-> message-body
        parse-message
        (get-document "compare")
        calculate-stats
        (save-document "compare"))))

(defn paragraph-consumer [message-body]
  (let [edit-id message-body
        edit-doc (get-document "edit" edit-id)
        account-id (edit-doc :account_id)
        doc-id (edit-doc :edited_document_id)]
    (update-paragraph edit-doc)
    (queue-doc "frequency.queue" account-id doc-id)
    (delete-doc "edit" edit-id)))

(comment
  (defn paragraph-consumer
    "Process incoming messages from the paragraph queue"
    [message-body]
    (-> message-body
        parse-message
        (get-document "edit")
        update-paragraph
        (publish-to-queue "frequency.queue")
        (delete-doc "edit"))))
