(ns rewryte.rabbit
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [langohr.consumers :as lc]))

(def rewryte-broker {:uri (get (System/getenv) "CLOUDAMQP_URL" "amqp://guest:guest@127.0.0.1")})

(defn declare-queue
  "Declare a new rabbit-mq queue"
  [channel queue-name]
  (lq/declare channel queue-name :auto-delete false :exclusive false))

(defn start-consumer
  "Start up a consumer for a given queue"
  [queue-name consumer-function]
  (let [connection (rmq/connect rewryte-broker)
        channel (lch/open connection)
        handler (fn [channel metadata ^bytes payload]
                  (consumer-function (String. payload)))]
    (declare-queue channel queue-name)
    (lc/subscribe channel queue-name handler :auto-ack true)))

(defn rabbit-publish
  "Publish the given content to a rabbit-mq queue"
  [queue-name content]
  (let [connection (rmq/connect rewryte-broker)
        channel (lch/open connection)]
    (lb/publish channel "" queue-name content :content-type "text/plain")))

(defn send-results-published
  "Notify rabbitmq that the account results are ready"
  [account-id doc-name]
  (let [queue-name (str account-id "-response.queue")]
    (rabbit-publish rewryte-broker queue-name doc-name)))

(defn queue-doc-compare
  "Queue a doc for comparison"
  [account-id doc-name]
  (let [queue-name "compare.queue"
        message (str account-id ":" doc-name)]
    (rabbit-publish rewryte-broker queue-name message)))