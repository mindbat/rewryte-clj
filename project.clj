(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.novemberain/langohr "1.4.1"]
                 [com.novemberain/monger "1.5.0"]
                 [cheshire "5.2.0"]
                 [clojure-opennlp "0.2.0"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar")
