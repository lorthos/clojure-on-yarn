(ns cljyarn.kafka.container
  (:gen-class)
  (:require [cljyarn.kafka.serde :as serde :reload-all true]
            [clojurewerkz.propertied.properties :as p]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log])
  (:import (org.apache.kafka.streams.kstream KStreamBuilder ValueMapper)
           (org.apache.kafka.streams KafkaStreams)))

(defn refine-config
  "refine config and make it ready to be used by kafka streams
  (resolve decorders etc..)"
  [conf
   kser
   vser]
  (-> conf
      (assoc "key.serde" (->> kser
                              .getClass
                              .getName))
      (assoc "value.serde" (->> vser
                                .getClass
                                .getName))))

(defn make-streams [conf
                    kser
                    vser
                    in-topic
                    out-topic
                    map-xf]
  (let [ref-conf (refine-config conf kser vser)
        p (p/load-from ref-conf)
        builder (KStreamBuilder.)
        stream (.stream builder
                        kser
                        vser
                        (into-array [in-topic]))]
    (do
      (-> stream
          (.mapValues ^ValueMapper
                      (reify ValueMapper
                        (apply [this value]
                          (map-xf value))))
          (.to out-topic))
      (KafkaStreams. builder p))))

;; hypothetical transformation
(def mapxf (comp #(.toUpperCase %)
                 #(str % "_suffix")))

(def cli-options
  [[nil "--kafka HOST:PORT" "bootstrap kafka servers"
    :default "localhost:9092"]
   [nil "--zk HOST:PORT" "zookeeper servers"
    :default "localhost:2181"]
   [nil "--src TOPIC" "source topic"
    :default "test1"]
   [nil "--dest TOPIC" "destination topic"
    :default "test2"]
   ])

(defn -main
  "Container Main"
  [& args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options)]
    (log/infof "Submitting app with option: %s" options)
    (let [streams
          (make-streams {"application.id"    "kafka-streams-clojure"
                         "bootstrap.servers" (:kafka options)
                         :ZOOKEEPER_CONNECT  (:zk options)}
                        (serde/string)
                        (serde/string)
                        (:src options)
                        (:dest options)
                        mapxf)]
      (.start streams)
      (.addShutdownHook (Runtime/getRuntime) (Thread. #(.stop streams))))))