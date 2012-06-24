(ns stylo.core
  (:use com.mefesto.wabbitmq))

(def stylo-broker {:host "localhost" :username "guest" :password "guest"})

(with-broker stylo-broker 
  (with-channel
    (exchange-declare "reverse.exchange" "direct")
    (queue-declare "reverse.queue")
    (queue-bind "reverse.queue" "reverse.exchange" "reverse")))

(with-broker stylo-broker 
  (with-channel
    (exchange-declare "general-response.exchange" "direct")
    (queue-declare "general-response.queue")
    (queue-bind "general-response.queue" "general-response.exchange" "general-response")))

(defn send-reverse
  "Reverse the incoming string and send it back to the message queue"
  [original]
  (with-broker stylo-broker
    (with-channel
      (with-exchange "general-response.exchange"
        (publish "general-response" (.getBytes (apply str (reverse original))))))))

(defn reverse-consumer []
  (with-broker stylo-broker 
    (with-channel
      (with-queue "reverse.queue"
        (doseq [msg (consuming-seq true)] ; consumes messages with auto-acknowledge enabled
          (send-reverse (String. (:body msg))))))))

(defn freq-consumer []
  (with-broker stylo-broker
    (with-channel
      (with-queue "frequency.queue"
        (doseq [msg (consuming-seq true)]
          (send-frequency (String. (:body msg))))))))
