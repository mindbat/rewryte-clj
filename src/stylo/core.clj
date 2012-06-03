(ns stylo.core
  (:use com.mefesto.wabbitmq))

(def test-broker {:host "localhost" :username "guest" :password "guest"})

(with-broker test-broker 
  (with-channel
    (exchange-declare "test.exchange" "direct")
    (queue-declare "test.queue")
    (queue-bind "test.queue" "test.exchange" "test")))

(with-broker test-broker 
  (with-channel
    (exchange-declare "test-response.exchange" "direct")
    (queue-declare "test-response.queue")
    (queue-bind "test-response.queue" "test-response.exchange" "test-response")))

(defn test-consumer []
  (with-broker test-broker 
    (with-channel
      (with-queue "test.queue"
        (doseq [msg (consuming-seq true)] ; consumes messages with auto-acknowledge enabled
          (send-reverse (String. (:body msg)))

(defn send-reverse
  "Reverse the incoming string and send it back to the message queue"
  [original]
  (with-broker test-broker
    (with-channel
      (with-exchange "test-response.exchange"
        (publish "test-response" (.getBytes (apply str (reverse original))))))))
