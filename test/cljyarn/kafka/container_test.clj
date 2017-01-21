(ns cljyarn.kafka.container-test
  (:require [clojure.test :refer :all]
            [cljyarn.kafka.container :refer :all]
            [cljyarn.kafka.serde :as serde]))

(deftest classpath-test
  (testing "classpath generation"
    (is (=
          {"bootstrap.servers" "localhost:9092"
           "key.serde"         "org.apache.kafka.common.serialization.Serdes$StringSerde"
           "value.serde"       "org.apache.kafka.common.serialization.Serdes$StringSerde"
           :APPLICATION_ID     "kafka-streams-clojure"
           :ZOOKEEPER_CONNECT  "localhost:2181"}

          (refine-config {:APPLICATION_ID     "kafka-streams-clojure"
                          "bootstrap.servers" "localhost:9092"
                          :ZOOKEEPER_CONNECT  "localhost:2181"}
                         (serde/string)
                         (serde/string))))))



