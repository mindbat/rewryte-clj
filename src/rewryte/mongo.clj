(ns rewryte.mongo
  (:use mongoika))

(defn save-results
  "Save the given results to mongodb"
  [user-id doc-name results]
  (with-mongo [connection {:host "127.0.0.1" :port 27107}]
    (with-db-binding (database connection :data)
      (update! :$set {:results results} (restrict :user_id user-id :document_name doc-name :docs)))))

(defn get-document
  "Fetch the given document from mongodb"
  [user-id doc-name])
