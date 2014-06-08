(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.2.0"]
                 [clj-aws-s3 "0.3.7"]
                 [clj-tika "1.2.0-snapshot"]
                 [clj-time "0.6.0"]
                 [clojure-opennlp "0.2.0"]
                 [com.novemberain/langohr "1.4.1"]
                 [korma "0.3.0-RC6"]
                 [org.apache.tika/tika-parsers "1.4"]
                 [org.clojure/tools.logging "0.3.0"]
                 [org.postgresql/postgresql "9.2-1004-jdbc4"]
                 [org.slf4j/slf4j-api "1.6.6"]
                 [org.slf4j/slf4j-log4j12 "1.6.6"]
                 [org.slf4j/jcl-over-slf4j "1.6.6"]
                 [org.slf4j/jul-to-slf4j "1.6.6"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar"
  :aot [rewryte.core])
