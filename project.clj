(defproject rewryte "0.3.0-SNAPSHOT"
  :description "Application for calculating writing style"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.novemberain/langohr "1.0.0-beta10"]
                 [com.novemberain/monger "1.1.2"]
                 [cheshire "4.0.1"]]
  :main rewryte.core
  :uberjar-name "rewryte.jar")
