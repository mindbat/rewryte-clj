(ns rewryte.mongo
  (:require [monger.core :as mcore]
            [monger.collection :as mcoll]))

(def mongo-host {:host "127.0.0.1" :port 27017})

(defn save-results
  "Save the given results to mongodb"
  [account-id doc-name results-string results-map]
  (let [doc-match {:account_id account-id :document_name doc-name}
        doc-update {:frequencies results-map :results results-string}]
    (mcore/connect! mongo-host)
    (mcore/set-db! (mcore/get-db "docs"))
    (mcoll/update "account" doc-match {:$set doc-update})))

(defn get-document
  "Fetch the given document from mongodb"
  [account-id doc-name]
  (let [doc-match {:account_id account-id :document_name doc-name}]
    (mcore/connect! mongo-host)
    (mcore/set-db! (mcore/get-db "docs"))
    (mcoll/find-one-as-map "account" doc-match)))
