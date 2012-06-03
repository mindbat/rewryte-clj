(ns stylo.core
  (:use com.mefesto.wabbitmq))

(use 'com.mefesto.wabbitmq)

(with-broker {:host "localhost" :username "guest" :password "guest"}
  (with-channel
    (exchange-declare "test.exchange" "direct")
    (queue-declare "test.queue")
    (queue-bind "test.queue" "test.exchange" "test")))

(with-broker {:host "localhost" :username "guest" :password "guest"}
  (with-channel
    (with-exchange "test.exchange"
      (publish "test" (.getBytes "Hello world!")))))

(defn consumer []
  (with-broker {:host "localhost" :username "guest" :password "guest"}
    (with-channel
      (with-queue "test.queue"
        (doseq [msg (consuming-seq true)] ; consumes messages with auto-acknowledge enabled
          (println "received:" (String. (:body msg))))))))
