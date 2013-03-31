(ns rewryte.consumer
  (:use rewryte.calc.stats, rewryte.calc.edits, rewryte.db, rewryte.message)
  (:require [clojure.string :as clj-str]))

(defn parse-message
  "Parse an incoming message"
  [message-body]
  (let [split-body (clj-str/split message-body #":")]
    {:account_id (first split-body) :doc_id (second split-body)}))

(defn url-safe
  "Generate a url-safe version of the string"
  [text]
  (clj-str/replace (clj-str/lower-case text) #"[^0-9a-zA-Z]" "_"))

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
