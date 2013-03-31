(ns rewryte.consumer
  (:use rewryte.calc.stats, rewryte.calc.edits, rewryte.db, rewryte.message)
  (:require [clojure.string :as clj-str]))

(defn parse-message
  "Parse an incoming message"
  [message-body]
  (let [split-body (clj-str/split message-body #":")]
    {:account_id (first split-body) :doc_id (second split-body)}))

(defn add-url-name
  "Add the url name for the document"
  [doc-map]
  (assoc doc-map :url_name (url-safe (:document_name doc-map))))

(defn frequency-consumer
  "Process incoming messages from the frequency queue"
  [message-body]
  (->> message-body
      parse-message
      (get-document "account")
      calculate-stats
      calculate-edits
      add-url-name
      (save-document "account")
      publish-results))

(comment (defn compare-consumer [message-body]
  (let [document-id message-body
        compare-doc (get-document "compare" document-id)
        document (cleanup-text (compare-doc :document))
        frequencies (count-words document)
        results (vec (sort-by val > frequencies))
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-compare-results document-id frequencies results sentence-length paragraph-length-words paragraph-length-sentences))))

(defn compare-consumer
  "Process incoming messages from the compare queue"
  [message-body]
  (->> message-body
      parse-message
      (get-document "compare")
      calculate-stats
      (save-document "compare")))

(defn paragraph-consumer [message-body]
  (let [edit-id message-body
        edit-doc (get-document "edit" {:doc_id edit-id})
        account-id (edit-doc :account_id)
        doc-id (edit-doc :edited_document_id)]
    (update-paragraph edit-doc)
    (queue-doc "frequency.queue" account-id doc-id)
    (delete-doc "edit" edit-id)))

(comment (defn paragraph-consumer
    "Process incoming messages from the paragraph queue"
    [message-body]
    (->> message-body
        parse-message
        (get-document "edit")
        update-paragraph
        (publish-to-queue "frequency.queue")
        (delete-doc "edit"))))
