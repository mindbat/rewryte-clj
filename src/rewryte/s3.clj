(ns rewryte.s3
  (:require [aws.sdk.s3 :as s3]
            [clojure.tools.logging :as log]))

(def cred {:access-key "AKIAIZ6UOCTKGD2V2XCA"
           :secret-key "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"})

(defn fetch-s3-document
  "Fetches a doc from s3 as an input stream."
  [bucket s3-id]
  (log/info "fetching s3 doc" bucket s3-id)
  (assoc {:bucket bucket
          :s3-id s3-id}
    :input-stream (:content (s3/get-object cred bucket s3-id))))

(defn save-plain-text-doc
  [bucket doc-map]
  (log/info "saving plain text doc" bucket (:s3-id doc-map))
  (s3/put-object cred bucket (:s3-id doc-map) (:text doc-map))
  doc-map)

(defn fetch-all-documents
  [bucket]
  (for [object (:objects (s3/list-objects cred bucket))]
    (fetch-s3-document bucket (:key object))))
