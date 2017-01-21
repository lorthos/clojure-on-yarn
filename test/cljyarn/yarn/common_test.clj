(ns cljyarn.yarn.common-test
  (:require [clojure.test :refer :all]
            [cljyarn.yarn.common :refer :all]
            [cljyarn.yarn.common :as c])
  (:import (org.apache.hadoop.yarn.conf YarnConfiguration)
           (java.util HashMap)))

(deftest classpath-generation-test
  (testing "classpath generation"
    (is (= {"CLASSPATH"
            "{{CLASSPATH}}<CPS>./*<CPS>{{HADOOP_CONF_DIR}}<CPS>{{HADOOP_COMMON_HOME}}/share/hadoop/common/*<CPS>{{HADOOP_COMMON_HOME}}/share/hadoop/common/lib/*<CPS>{{HADOOP_HDFS_HOME}}/share/hadoop/hdfs/*<CPS>{{HADOOP_HDFS_HOME}}/share/hadoop/hdfs/lib/*<CPS>{{HADOOP_YARN_HOME}}/share/hadoop/yarn/*<CPS>{{HADOOP_YARN_HOME}}/share/hadoop/yarn/lib/*<CPS>./log4j.properties"}
           (let [env (HashMap.)]
             (c/setup-classpath! env (YarnConfiguration.))
             env)))))


(def args ["--asd=1" "qwe" "--cores=2"])
(deftest cli-options-test
  (testing "cli parsing"
    (is (=
          {:arguments ["qwe"]
           :errors    ["Unknown option: \"--asd\""]
           :options   {:container-main "cljyarn.kafka.container"
                       :containers     1
                       :cores          2
                       :jar            "cljyarn-0.1.0-SNAPSHOT-standalone.jar"
                       :mem            256}}
          (-> (clojure.tools.cli/parse-opts args cli-options)
              (dissoc :summary))))
    )
  (testing "interleave"
    (is (= "--asd=1 qwe --cores=2"
           (interleave-args args)))
    ))

