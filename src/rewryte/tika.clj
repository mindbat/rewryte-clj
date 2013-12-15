(ns rewryte.tika
  (:require [clj-tika.core :as tika])
  (:import [java.io File]))

(def relevant-keys #{:text :creation-date :word-count :paragraph-count})

(defn get-document-name
  [^File file]
  (second (re-find #"([a-zA-Z]+)\.[a-z]+" (.getName file))))

(defn extract-text
  [thing]
  (tika/parse thing))
