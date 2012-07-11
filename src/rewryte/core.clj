(ns rewryte.core
  (:gen-class :main true)
  (:use com.mefesto.wabbitmq, rewryte.process, clojure.string))

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

(defn send-frequency
  "Calculate the word frequencies in the incoming string and publish results to queue."
  [original]
  (with-broker rewryte-broker
    (with-channel
      (with-exchange "general-response.exchange"
        (publish "general-response" (.getBytes (apply str (count-words original))))))))

(defn freq-consumer []
  (with-broker rewryte-broker
    (with-channel
      (with-queue "frequency.queue"
        (doseq [msg (consuming-seq true)]
          (send-frequency (String. (:body msg))))))))

(defn -main [consumer]
  (cond
    (= consumer "frequency") (freq-consumer)
    :else (println "No consumer by that name available")))
