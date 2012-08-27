(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, clojure.string, cheshire.core))

(def rewryte-broker {:host "localhost" :username "guest" :password "guest"})

(declare-queue rewryte-broker "general-response.exchange" "general-response.queue" "general-response")

(declare-queue rewryte-broker "frequency.exchange" "frequency.queue" "frequency")

(defn send-results-published
  "Notify rabbitmq that the account results are ready"
  [account-id doc-name]
  (let [exchange-name (str account-id "-response.exchange")
        queue-name (str account-id "-response.queue")
        short-name (str account-id "-response")]
    (declare-queue rewryte-broker exchange-name queue-name short-name)
    (rabbit-publish rewryte-broker exchange-name short-name doc-name)))

(defn frequency-consumer [message-body]
  (let [split-body (split message-body #":")
        account-id (Integer/parseInt (first split-body))
        doc-name (second split-body)
        mongo-doc (get-document account-id doc-name)
        document (mongo-doc :document)
        results-map (count-words document)
        results (generate-string results-map)
        pages-seq (paginate document)
        pages (generate-string (zipmap (range (count pages)) pages))
        sentence-length (avg-sentence-length document)
        paragraph-length-words (avg-paragraph-length-words document)
        paragraph-length-sentences (avg-paragraph-length-sentences document)]
    (save-results account-id doc-name results results-map pages sentence-length paragraph-length-words paragraph-length-sentences)
    (send-results-published account-id doc-name)))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (start-consumer rewryte-broker "frequency.queue" frequency-consumer)
    :else (println "No consumer by that name available")))
