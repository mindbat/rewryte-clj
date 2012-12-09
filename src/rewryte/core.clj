(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, rewryte.compare, clojure.string, cheshire.core))

(defn frequency-consumer [message-body]
  (let [split-body (split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-name (second split-body)
        mongo-doc (get-document account-id doc-name)
        document (mongo-doc :document)
        url-name (url-safe doc-name)
        frequencies (count-words document)
        freq-standard (count-words (remove-fluff document))
        max-frequency-full (apply max (vals frequencies))
        results-full (vec (sort-by val > frequencies))
        max-frequency-standard (apply max (vals freq-standard))
        results-standard (vec (sort-by val > freq-standard))
        pages (paginate document)
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-results account-id doc-name url-name results-full results-standard frequencies max-frequency-full max-frequency-standard pages sentence-length paragraph-length-words paragraph-length-sentences)
    (queue-doc-compare account-id doc-name)))

(defn compare-consumer [message-body]
  (let [split-body (split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-name (second split-body)
        url-name (url-safe doc-name)
        mongo-doc (get-document account-id doc-name)
        score (compute-score mongo-doc "perfect.queue")]
    (save-score account-id doc-name score)
    (send-results-published account-id url-name)))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (do
                                (future (start-consumer "frequency.queue" frequency-consumer))
                                (future (start-consumer "compare.queue" compare-consumer)))
    :else (println "No consumer by that name available")))
