(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [clj-aws-s3 "0.3.7"]
                 [clj-tika "1.2.0-snapshot"]
                 [clj-time "0.6.0"]
                 [clojure-opennlp "0.2.0"]
                 [com.novemberain/langohr "1.4.1"]
                 [org.apache.tika/tika-parsers "1.4"]
                 [korma "0.3.0-RC6"]
                 [org.postgresql/postgresql "9.2-1004-jdbc4"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar")
