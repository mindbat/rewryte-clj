(ns rewryte.db
  (:require [monger.core :as mcore]
            [monger.collection :as mcoll]
            [monger.operators :refer [$set]])
  (:import com.mongodb.WriteConcern [org.bson.types ObjectId]))

(def mongo-host (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1:27017/docs"))

(defn connect-to-doc-db!
  "Connect to the database holding documents for processing"
  []
  (mcore/connect-via-uri! mongo-host))

(defn create-document
  "Create a new document in mongo db"
  [collection document]
  (mcoll/insert-and-return collection (merge document {:_id (ObjectId.)})))

(defn update-document
  "Save the given document to the db"
  [coll-name doc-map]
  (let [doc-match {:_id (:_id doc-map)}
        doc-update (dissoc doc-map :account_id :_id :document :document_name)]
    (mcoll/update coll-name doc-match {$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)
    doc-map))

(defn save-results
  "Save the given results to mongodb"
  [account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard paragraphs longest-sentences most-adverbs sentence-length paragraph-length-words paragraph-length-sentences]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:frequencies frequencies :url_name url-name :results_full {:max_frequency max-frequency-full, :results results-full} :results_standard {:max_frequency max-frequency-standard, :results results-standard} :paragraphs paragraphs :longest_sentences longest-sentences :most_adverbs most-adverbs :sentence_length sentence-length :paragraph_length_words paragraph-length-words :paragraph_length_sentences paragraph-length-sentences}]
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn get-document
  "Fetch the given document from mongodb"
  [coll-name doc-map]
  (let [doc-match {:_id (ObjectId. (:doc_id doc-map))}]
    (mcoll/find-one-as-map coll-name doc-match)))

(defn search-collection
  "Execute the given search query against the given collection"
  [query collection]
  (let [mongo-db (mcore/get-db "docs")]
    (mcoll/find-maps collection query)))

(defn update-paragraph
  "Update the paragraph text for a given mongo document"
  [edit-doc]
  (let [edited-doc-id (edit-doc :edited_document_id)
        account-id (edit-doc :account_id)
        original-doc (get-document "account" {:doc_id edited-doc-id})
        paragraphs (original-doc :paragraphs)
        edited-index (edit-doc :paragraph_number)
        new-text (edit-doc :new_text)
        updated-paragraphs (assoc paragraphs edited-index new-text)
        updated-doc-text (clojure.string/join "\n\n" updated-paragraphs)
        doc-match {:account_id account-id :_id (ObjectId. edited-doc-id)}
        doc-update {:document updated-doc-text}]
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn delete-doc
  "Delete the given document from mongo-db"
  [collection-name doc-id]
  (let [oid (ObjectId. doc-id)]
    (mcoll/remove-by-id collection-name oid)))

(defn find-s3-document
  [account-id s3-id]
  (let [doc-match {:account_id account-id :s3_id s3-id}]
    (mcoll/find-one-as-map "account" doc-match)))

(defn save-doc-text
  "Save the new document text to mongodb"
  [account-id s3-id document]
  (let [existing-doc (find-s3-document account-id s3-id)
        new-doc (merge existing-doc {:document (:text document)})]
    (mcoll/update "account"
                  {:_id (:_id existing-doc)}
                  new-doc
                  :write-concern WriteConcern/JOURNAL_SAFE)
    (.toString (:_id new-doc))))
