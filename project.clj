(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/langohr "1.4.1"]
                 [com.novemberain/monger "1.7.0"]
                 [cheshire "5.2.0"]
                 [clj-tika "1.2.0-snapshot"]
                 [clojure-opennlp "0.2.0"]
                 [org.apache.tika/tika-parsers "1.4"]
                 [clj-aws-s3 "0.3.7"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar")
