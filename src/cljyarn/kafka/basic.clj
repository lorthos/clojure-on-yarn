(ns cljyarn.kafka.basic
  "A simple application, can be run as a container on YARN"
  (:gen-class))

(defn -main [& args]
  (Thread/sleep 30000)
  (println "blazing fast container"))
