(ns rewryte.core
  (:gen-class :main true)
  (:use com.mefesto.wabbitmq, rewryte.process, rewryte.mongo, clojure.string, cheshire.core))

(def rewryte-broker {:host "localhost" :username "guest" :password "guest"})

(with-broker rewryte-broker 
  (with-channel
    (exchange-declare "general-response.exchange" "direct")
    (queue-declare "general-response.queue")
    (queue-bind "general-response.queue" "general-response.exchange" "general-response")))

(with-broker rewryte-broker 
  (with-channel
    (exchange-declare "frequency.exchange" "direct")
    (queue-declare "frequency.queue")
    (queue-bind "frequency.queue" "frequency.exchange" "frequency")))

(defn declare-queue
  "Declare a new rabbit-mq queue"
  [exchange-name queue-name short-name]
  (with-broker rewryte-broker
    (with-channel
      (exchange-declare exchange-name "direct")
      (queue-declare queue-name)
      (queue-bind queue-name exchange-name short-name))))

(defn send-results-published
  "Notify rabbitmq that the user results are ready"
  [user-id doc-name]
  (let [exchange-name (str user-id "-response.exchange")
        queue-name (str user-id "-response.queue")
        short-name (str user-id "-response")]
    (declare-queue exchange-name queue-name short-name)
    (with-broker rewryte-broker
      (with-channel
        (with-exchange exchange-name 
          (publish short-name (.getBytes doc-name)))))))

(defn freq-consumer []
  (with-broker rewryte-broker
    (with-channel
      (with-queue "frequency.queue"
        (doseq [msg (consuming-seq true)]
          (let [body (String. (:body msg))
                split-body (split body #":")
                user-id (Integer/parseInt (first split-body))
                doc-name (second split-body)
                mongo-doc (get-document user-id doc-name)
                document (mongo-doc :document)
                results (generate-string (count-words document))]
            (save-results user-id doc-name results)
            (send-results-published user-id doc-name)))))))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (freq-consumer)
    :else (println "No consumer by that name available")))
