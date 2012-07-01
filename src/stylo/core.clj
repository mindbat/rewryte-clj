(ns stylo.core
  (:gen-class :main true)
  (:use com.mefesto.wabbitmq, stylo.process, clojure.string))

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

(with-broker stylo-broker 
  (with-channel
    (exchange-declare "frequency.exchange" "direct")
    (queue-declare "frequency.queue")
    (queue-bind "frequency.queue" "frequency.exchange" "frequency")))

(with-broker stylo-broker 
  (with-channel
    (exchange-declare "scramble.exchange" "direct")
    (queue-declare "scramble.queue")
    (queue-bind "scramble.queue" "scramble.exchange" "scramble")))

(defn send-reverse
  "Reverse the incoming string and send it back to the message queue"
  [original]
  (with-broker stylo-broker
    (with-channel
      (with-exchange "general-response.exchange"
        (publish "general-response" (.getBytes (apply str (reverse original))))))))

(defn send-frequency
  "Calculate the word frequencies in the incoming string and publish results to queue."
  [original]
  (with-broker stylo-broker
    (with-channel
      (with-exchange "general-response.exchange"
        (publish "general-response" (.getBytes (apply str (count-words original))))))))

(defn send-scramble
  "Scramble all the words in the given text"
  [original]
  (with-broker stylo-broker
    (with-channel
      (with-exchange "general-response.exchange"
        (publish "general-response" (.getBytes (join " " (scramble-words original))))))))

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

(defn scramble-consumer []
  (with-broker stylo-broker
    (with-channel
      (with-queue "scramble.queue"
        (doseq [msg (consuming-seq true)]
          (send-scramble (String. (:body msg))))))))

(defn -main [consumer]
  (cond
    (= consumer "reverse") (reverse-consumer)
    (= consumer "frequency") (freq-consumer)
    (= consumer "scramble") (scramble-consumer)
    (= consumer "all") (dorun (pcalls reverse-consumer freq-consumer scramble-consumer))
    :else (println "No consumer by that name available")))
