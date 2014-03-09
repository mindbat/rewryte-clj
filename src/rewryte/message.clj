(ns rewryte.message
  (:require [langohr.core :as rmq]
            [langohr.channel :as lch]
            [langohr.queue :as lq]
            [langohr.basic :as lb]
            [langohr.consumers :as lc]))

(def rewryte-broker {:uri (get (System/getenv) "RABBITMQ_URL" "amqp://guest:guest@127.0.0.1")})

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
                  (try (consumer-function (String. payload))
                       (catch Exception ex (println (.getMessage ex)))))]
    (declare-queue channel queue-name)
    (lc/subscribe channel queue-name handler :auto-ack true)))

(defn rabbit-publish
  "Publish the given content to a rabbit-mq queue"
  [queue-name content]
  (let [connection (rmq/connect rewryte-broker)
        channel (lch/open connection)]
    (declare-queue channel queue-name)
    (lb/publish channel "" queue-name content :content-type "text/plain")))

(defn publish-results
  "Notify any listeners that the document results are ready"
  [{:keys [account_id s3-id] :as doc-map}]
  (rabbit-publish (str account_id "-response.queue") s3-id))

(defn queue-doc
  "Queue a document for further processing"
  [queue-name account-id doc-id]
  (rabbit-publish queue-name (str account-id ":" doc-id)))
