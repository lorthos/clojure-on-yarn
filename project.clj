(defproject cljyarn "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-streams "0.10.0.0"]
                 [clojurewerkz/propertied "1.2.0"]
                 [org.apache.hadoop/hadoop-yarn-client "2.7.1"]
                 [org.apache.hadoop/hadoop-common "2.7.1"]
                 [org.clojure/tools.cli "0.3.5"]

                 [com.stuartsierra/component "0.3.1"]

                 ;tracking ui
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [ring "1.4.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.6"]


                 ;batch
                 ;dist
                 [org.apache.hadoop/hadoop-client "2.7.1"]
                 [org.apache.hadoop/hadoop-common "2.7.1"]
                 ;Todo test scope
                 [org.apache.hadoop/hadoop-hdfs "2.7.1" :classifier "tests"]
                 [org.apache.hadoop/hadoop-common "2.7.1" :classifier "tests"]
                 ]
  :warn-on-reflection true
  :java-source-paths ["src-java"]
  :aot :all
  :main cljyarn.yarn.submit
  :uberjar-exclusions [#"META-INF/LICENSE" #"META-INF/license" #"license/*"]
  )
