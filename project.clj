(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.mefesto/wabbitmq "0.2.1"]
                 [com.novemberain/monger "1.1.2"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar")
