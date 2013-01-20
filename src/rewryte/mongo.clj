(ns rewryte.mongo
  (:require [monger.core :as mcore]
            [monger.collection :as mcoll])
  (:import com.mongodb.WriteConcern [org.bson.types ObjectId]))

(def mongo-host (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1:27017/docs"))

(defn save-results
  "Save the given results to mongodb"
  [account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard paragraphs longest-sentences sentence-length paragraph-length-words paragraph-length-sentences]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:frequencies frequencies :url_name url-name :results_full {:max_frequency max-frequency-full, :results results-full} :results_standard {:max_frequency max-frequency-standard, :results results-standard} :paragraphs paragraphs :longest_sentences longest-sentences :sentence_length sentence-length :paragraph_length_words paragraph-length-words :paragraph_length_sentences paragraph-length-sentences}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn get-document
  "Fetch the given document from mongodb"
  [account-id doc-id]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/find-one-as-map "account" doc-match)))

(defn get-edit-doc
  "Fetch the given edited paragraph document from mongodb"
  [edit-id]
  (let [doc-match {:_id (ObjectId. edit-id)}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/find-one-as-map "edit" doc-match)))

(defn search-collection
  "Execute the given search query against the given collection"
  [query collection]
  (let [mongo-db (mcore/get-db "docs")]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/find-maps collection query)))

(defn save-score
  "Save the doc score to mongodb"
  [account-id doc-id score]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:score score}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn update-paragraph
  "Update the paragraph text for a given mongo document"
  [edit-doc]
  (let [edited-doc-id (edit-doc :edited_document_id)
        account-id (edit-doc :account_id)
        original-doc (get-document account-id edited-doc-id)
        paragraphs (original-doc :paragraphs)
        edited-index (edit-doc :paragraph_number)
        new-text (edit-doc :new_text)
        updated-paragraphs (assoc paragraphs edited-index new-text)
        updated-doc-text (clojure.string/join "\n\n" updated-paragraphs)
        doc-match {:account_id account-id :_id (ObjectId. edited-doc-id)}
        doc-update {:document updated-doc-text}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn delete-doc
  "Delete the given document from mongo-db"
  [collection-name doc-id]
  (let [oid (ObjectId. doc-id)]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/remove-by-id collection-name oid)))