(ns rewryte.rabbit
  (:use com.mefesto.wabbitmq))

(defn declare-queue
  "Declare a new rabbit-mq queue"
  [broker-map exchange-name queue-name short-name]
  (with-broker broker-map 
    (with-channel
      (exchange-declare exchange-name "direct")
      (queue-declare queue-name)
      (queue-bind queue-name exchange-name short-name))))

(defn start-consumer
  "Start up a consumer for a given queue"
  [broker-map queue-name consumer-function]
  (with-broker broker-map
    (with-channel
      (with-queue queue-name
        (doseq [msg (consuming-seq true)]
          (try
            (consumer-function (String. (:body msg)))
            (catch Exception e (.printStackTrace e))))))))

(defn rabbit-publish
  "Publish the given content to a rabbit-mq queue"
  [broker-map exchange-name short-name content]
  (with-broker broker-map
    (with-channel
      (with-exchange exchange-name
        (publish short-name (.getBytes content))))))
