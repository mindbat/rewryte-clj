(ns rewryte.tika
  (:require [clj-tika.core :as tika]))

(defn extract-text
  [file]
  (:text (tika/parse file)))
