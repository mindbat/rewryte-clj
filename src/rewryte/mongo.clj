(ns rewryte.mongo
  (:require [monger.core :as mcore]
            [monger.collection :as mcoll])
  (:import com.mongodb.WriteConcern [org.bson.types ObjectId]))

(defn save-results
  "Save the given results to mongodb"
  [account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard paragraphs longest-sentences most-adverbs sentence-length paragraph-length-words paragraph-length-sentences]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:frequencies frequencies :url_name url-name :results_full {:max_frequency max-frequency-full, :results results-full} :results_standard {:max_frequency max-frequency-standard, :results results-standard} :paragraphs paragraphs :longest_sentences longest-sentences :most_adverbs most-adverbs :sentence_length sentence-length :paragraph_length_words paragraph-length-words :paragraph_length_sentences paragraph-length-sentences}]
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn make-compare-doc
  "Make a compare doc map for mongodb"
  [freq results sentences para-words para-sentences]
  {:frequencies freq
   :results results
   :sentence_length sentences
   :paragraph_length_words para-words
   :paragraph_length_sentences para-sentences})

(defn save-compare-results
  "Save the results of the comparison document processing to mongodb"
  [document-id frequencies results sentence-length paragraph-length-words paragraph-length-sentences]
  (let [doc-match {:_id (ObjectId. document-id)}
        doc-update (make-compare-doc frequencies results sentence-length paragraph-length-words paragraph-length-sentences)]
    (mcoll/update "compare" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn get-document
  "Fetch the given document from mongodb"
  [coll-name doc-id]
  (let [doc-match {:_id (ObjectId. doc-id)}]
    (mcoll/find-one-as-map coll-name doc-match)))

(defn search-collection
  "Execute the given search query against the given collection"
  [query collection]
  (let [mongo-db (mcore/get-db "docs")]
    (mcoll/find-maps collection query)))

(defn save-score
  "Save the doc score to mongodb"
  [account-id doc-id score]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:score score}]
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn update-paragraph
  "Update the paragraph text for a given mongo document"
  [edit-doc]
  (let [edited-doc-id (edit-doc :edited_document_id)
        account-id (edit-doc :account_id)
        original-doc (get-document "account" edited-doc-id)
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