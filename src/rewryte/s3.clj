(ns rewryte.s3
  (:require [aws.sdk.s3 :as s3]))

(def cred {:access-key "AKIAIZ6UOCTKGD2V2XCA"
           :secret-key "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"})

(defn fetch-s3-document [s3-key]
  (:content (s3/get-object cred "rewryte-uploads" s3-key)))
