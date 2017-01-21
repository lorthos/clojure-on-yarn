(ns cljyarn.kafka.serde
  (:import (org.apache.kafka.common.serialization Serdes)))

(defn string []
  (Serdes/String))
