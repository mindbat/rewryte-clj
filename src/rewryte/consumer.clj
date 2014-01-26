(ns rewryte.consumer
  (:require [clojure.string :as str]
            [rewryte.calc.edits :refer [calculate-edits]]
            [rewryte.calc.stats :refer [calculate-stats]]
            [rewryte.db :refer [save-new-document
                                delete-doc
                                get-document
                                update-document
                                update-paragraph]]
            [rewryte.genre :refer [update-genre-training-data]]
            [rewryte.message :refer [publish-results queue-doc]]
            [rewryte.s3 :refer [fetch-s3-document]]
            [rewryte.tika :refer [extract-text]]))

(defn parse-message
  "Parse an incoming message"
  [message-body]
  (let [split-body (str/split message-body #":")]
    {:account_id (first split-body) :doc_id (second split-body)}))

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

(defn frequency-consumer
  "Process incoming messages from the frequency queue"
  [message-body]
  (->> message-body
      parse-message
      (get-document "account")
      calculate-stats
      calculate-edits
      add-url-name
      realize-map
      (update-document "account")
      publish-results))

(defn genre-train-consumer
  "Process incoming messages from the genre-training queue"
  [message-body]
  (->> message-body
       parse-message
       (get-document "genre")
       update-genre-training-data))

(defn paragraph-consumer [message-body]
  (let [edit-id message-body
        edit-doc (get-document "edit" {:doc_id edit-id})
        account-id (edit-doc :account_id)
        doc-id (edit-doc :edited_document_id)]
    (update-paragraph edit-doc)
    (queue-doc "frequency.queue" account-id doc-id)
    (delete-doc "edit" edit-id)))

(defn extract-consumer [message-body]
  (let [s3-id message-body
        account-id (first (str/split message-body #"-"))]
    (->> (fetch-s3-document s3-id)
         extract-text
         (save-new-document (Integer/parseInt account-id) s3-id)
         (queue-doc "frequency.queue" account-id))))

(comment (defn paragraph-consumer
    "Process incoming messages from the paragraph queue"
    [message-body]
    (->> message-body
        parse-message
        (get-document "edit")
        update-paragraph
        (publish-to-queue "frequency.queue")
        (delete-doc "edit"))))
