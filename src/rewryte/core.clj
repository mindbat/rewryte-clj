(ns rewryte.core
  (:gen-class :main true)
  (:use rewryte.rabbit, rewryte.process, rewryte.mongo, clojure.string, cheshire.core))

(def rewryte-broker {:host "localhost" :username "guest" :password "guest"})

(declare-queue rewyte-broker "general-response.exchange" "general-response.queue" "general-response")

(declare-queue rewyte-broker "frequency.exchange" "frequency.queue" "frequency")

(defn send-results-published
  "Notify rabbitmq that the user results are ready"
  [user-id doc-name]
  (let [exchange-name (str user-id "-response.exchange")
        queue-name (str user-id "-response.queue")
        short-name (str user-id "-response")]
    (declare-queue rewryte-broker exchange-name queue-name short-name)
    (rabbit-publish rewryte-broker exchange-name short-name doc-name)))

(defn frequency-consumer [message-body]
  (let [split-body (split body #":")
        user-id (Integer/parseInt (first split-body))
        doc-name (second split-body)
        mongo-doc (get-document user-id doc-name)
        document (mongo-doc :document)
        results (generate-string (count-words document))]
    (save-results user-id doc-name results)
    (send-results-published user-id doc-name)))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (start-consumer rewryte-broker "frequency.queue" frequency-consumer)
    :else (println "No consumer by that name available")))
