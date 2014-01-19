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

(defn find-s3-document
  [account-id s3-id]
  (let [doc-match {:account_id account-id :s3_id s3-id}]
    (mcoll/find-one-as-map "account" doc-match)))

(defn update-recommendations
  [existing new]
  (let [updated (merge existing new)]
    (mcoll/update-by-id "account" (:_id existing) updated)
    updated))

(defn save-recommendations
  "Save the new document text to mongodb"
  [{:keys [account-id s3-id] :as recommendations}]
  (let [existing-doc (find-s3-document account-id s3-id)]
    (if (empty? existing-doc)
      (create-document "account" recommendations)
      (update-recommendations existing-doc recommendations))))
