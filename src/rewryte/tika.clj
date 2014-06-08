(ns rewryte.tika
  (:require [clj-tika.core :as tika]
            [clojure.tools.logging :as log])
  (:import [java.io File]))

(def relevant-keys #{:text :creation-date :word-count :paragraph-count})

(defn get-document-name
  [^File file]
  (second (re-find #"([a-zA-Z]+)\.[a-z]+" (.getName file))))

(defn extract-text
  "Extracts the text from the input stream indicated in the doc-map"
  [doc-map]
  (log/info "extracting text from input stream")
  (merge (dissoc doc-map :input-stream) (tika/parse (:input-stream doc-map))))
