(ns rewryte.mongo
  (:require [monger.core :as mcore]
            [monger.collection :as mcoll])
  (:import com.mongodb.WriteConcern [org.bson.types ObjectId]))

(def mongo-host (get (System/getenv) "MONGOLAB_URI" "mongodb://127.0.0.1:27017/docs"))

(defn save-results
  "Save the given results to mongodb"
  [account-id doc-id url-name results-full results-standard frequencies max-frequency-full max-frequency-standard pages paragraphs longest-sentences sentence-length paragraph-length-words paragraph-length-sentences]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}
        doc-update {:frequencies frequencies :url_name url-name :results_full {:max_frequency max-frequency-full, :results results-full} :results_standard {:max_frequency max-frequency-standard, :results results-standard} :pages pages :paragraphs paragraphs :longest_sentences longest-sentences :sentence_length sentence-length :paragraph_length_words paragraph-length-words :paragraph_length_sentences paragraph-length-sentences}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/update "account" doc-match {:$set doc-update} :write-concern WriteConcern/JOURNAL_SAFE)))

(defn get-document
  "Fetch the given document from mongodb"
  [account-id doc-id]
  (let [doc-match {:account_id account-id :_id (ObjectId. doc-id)}]
    (mcore/connect-via-uri! mongo-host)
    (mcoll/find-one-as-map "account" doc-match)))

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
