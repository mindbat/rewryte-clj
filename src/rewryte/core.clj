(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, rewryte.compare, clojure.string, cheshire.core))

(def rewryte-broker {:host "tiger.cloudamqp.com" :virtual-host "app9174674_heroku.com" :username "app9174674_heroku.com" :password "gDiH-_Y2d-yfp8hhcacrouWgb45Hvd4g"})

(defn send-results-published
  "Notify rabbitmq that the account results are ready"
  [account-id doc-name]
  (let [exchange-name ""
        queue-name (str account-id "-response.queue")
        short-name (str account-id "-response")]
    (declare-queue rewryte-broker exchange-name queue-name short-name)
    (rabbit-publish rewryte-broker exchange-name short-name doc-name)))

(defn queue-doc-compare
  "Queue a doc for comparison"
  [account-id doc-name]
  (let [exchange-name ""
        queue-name "compare.queue"
        short-name "compare"
        message (str account-id ":" doc-name)]
    (declare-queue rewryte-broker exchange-name queue-name short-name)
    (rabbit-publish rewryte-broker exchange-name short-name message)))

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
        score (compute-score mongo-doc "perfect")]
    (save-score account-id doc-name score)
    (send-results-published account-id url-name)))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (do
                                (future (start-consumer rewryte-broker "frequency.queue" frequency-consumer))
                                (future (start-consumer rewryte-broker "compare.queue" compare-consumer)))
    :else (println "No consumer by that name available")))
