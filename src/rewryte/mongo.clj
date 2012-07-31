(ns rewryte.mongo
  (:require [monger.core :as mcore] 
            [monger.collection :as mcoll]))

(def mongo-host {:host "127.0.0.1" :port 27017})

(defn save-results
  "Save the given results to mongodb"
  [user-id doc-name results]
  (let [doc-match {:user_id user-id :document_name doc-name}
        doc-update {:results results}]
    (mcore/connect! mongo-host)
    (mcore/set-db! (mcore/get-db "data"))
    (mcoll/update "docs" doc-match {:$set doc-update})))

(defn get-document
  "Fetch the given document from mongodb"
  [user-id doc-name]
  (let [doc-match {:user_id user-id :document_name doc-name}]
    (mcore/connect! mongo-host)
    (mcore/set-db! (mcore/get-db "data"))
    (mcoll/find-maps "docs" doc-match))) 
