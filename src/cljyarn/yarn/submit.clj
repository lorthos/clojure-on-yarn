(ns cljyarn.yarn.submit
  "Yarn Client"
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.cli :refer [parse-opts]]
            [cljyarn.yarn.common :as c]
            [clojure.tools.cli :as cli]
            [cljyarn.yarn.config :as conf]
            [clojure.tools.logging :as log])
  (:import (org.apache.hadoop.yarn.client.api YarnClient YarnClientApplication)
           (org.apache.hadoop.yarn.api.records ApplicationSubmissionContext YarnApplicationState)
           (org.apache.hadoop.yarn.conf YarnConfiguration)
           (org.apache.hadoop.fs Path)))

(defn make-yarn-client
  "Create yarn client"
  [conf]
  (doto (YarnClient/createYarnClient)
    (.init conf)
    (.start)))

(defn make-yarn-client-app
  "create yarn app"
  [client]
  (.createApplication client))


(defn make-submission-context [^YarnClientApplication app
                               ^YarnConfiguration conf
                               ^Path jar-path
                               ^String jar-name
                               app-config
                               main-to-run]
  (doto ^ApplicationSubmissionContext (.getApplicationSubmissionContext app)
    (.setApplicationName "Clojure Kafka Streams")
    (.setQueue "default")
    (.setAMContainerSpec (c/make-launch-context conf jar-path jar-name (:am-memory app-config)
                                                main-to-run))
    (.setResource (c/make-res (:am-memory conf/props) (:am-cores conf/props)))))


(defn -main
  "
  parse the opts
  pass them to app master
  mem
  containers
  append any other to app master
  "
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args c/cli-options)
        jar-name (:jar options)
        conf (YarnConfiguration.)
        client (make-yarn-client conf)
        app (make-yarn-client-app client)
        app-context (make-submission-context app conf
                                             (c/get-hdfs-path jar-name)
                                             jar-name
                                             conf/props
                                             (str "clojure.main -m cljyarn.yarn.appmaster " (c/interleave-args args)))
        app-id (.getApplicationId app-context)
        ]
    (log/infof "Submitting app: %s with params: %s" app-id args)
    (.submitApplication client app-context)

    (let [app-report (atom (.getApplicationReport client app-id))
          app-state (atom (.getYarnApplicationState @app-report))]
      (while (empty? (filter #(= @app-state %) [YarnApplicationState/FAILED
                                                YarnApplicationState/KILLED
                                                YarnApplicationState/FINISHED]))
        (log/infof "Waiting for app...")
        (log/infof (.getOriginalTrackingUrl @app-report))
        (log/infof (.getTrackingUrl @app-report))
        (Thread/sleep 10000)
        (reset! app-report (.getApplicationReport client app-id))
        (reset! app-state (.getYarnApplicationState @app-report))
        )
      (log/infof "Client: Finished %s with state %s" app-id @app-state))))
