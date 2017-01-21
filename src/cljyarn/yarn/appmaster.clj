(ns cljyarn.yarn.appmaster
  "Application Master"
  (:require [cljyarn.yarn.common :as c]
            [cljyarn.yarn.tracking.ui :as ui]
            [cljyarn.yarn.config :as conf]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:gen-class)
  (:import (org.apache.hadoop.yarn.conf YarnConfiguration)
           (org.apache.hadoop.yarn.client.api.async AMRMClientAsync$CallbackHandler AMRMClientAsync)
           (org.apache.hadoop.yarn.client.api NMClient AMRMClient$ContainerRequest)
           (org.apache.hadoop.yarn.api.records Priority FinalApplicationStatus)
           (org.apache.hadoop.yarn.util Records)))


(defn make-callback-handler
  "make yarn callback handler, collbacks for container allocatiom and completion"
  [^YarnConfiguration conf ^NMClient nmclient container-atom
   mem jar-path jar-name main-to-run]
  (reify AMRMClientAsync$CallbackHandler
    (onContainersAllocated [this containers]
      (doall
        (map
          #(.startContainer nmclient % (c/make-launch-context
                                         conf
                                         jar-path
                                         jar-name
                                         mem
                                         main-to-run)) containers))
      (swap! ui/am-status assoc :containers-allocated containers)
      )

    (onContainersCompleted [this statuses]
      (doall
        (map (fn [status]
               (println status)
               (swap! container-atom dec)) statuses))
      (swap! ui/am-status assoc :containers-completed statuses))

    (getProgress [this] (float 0))))

(defn make-nm-client [conf]
  (doto (NMClient/createNMClient)
    (.init conf)
    (.start)))

(defn make-amrm-client [conf callback-handler]
  (doto (AMRMClientAsync/createAMRMClientAsync 1000 callback-handler)
    (.init conf)
    (.start)))

(defn make-priority []
  (doto ^Priority (Records/newRecord Priority)
    (.setPriority 0)))

(defn appmaster-system
  "component system for the appmaster process"
  [config-options]
  (let [{:keys [cli-options port]} config-options]
    (-> (component/system-map
          :config-options config-options
          :server (ui/make-tracking-server port cli-options)))))

(defn -main
  "Application Master Entry Point"
  [& args]
  (log/infof "Starting Appmaster: " args)
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args c/cli-options)
        container-main (:container-main options)
        jar-name (:jar options)
        no-containers (atom (:containers options))
        conf (YarnConfiguration.)
        nm-client (make-nm-client conf)
        callback-handler (make-callback-handler
                           conf nm-client no-containers (:mem options)
                           (c/get-hdfs-path jar-name) jar-name
                           (str "clojure.main -m " container-main " " (c/interleave-args args)))
        rm-client (make-amrm-client conf callback-handler)]
    (component/start (appmaster-system {:port (:tracking-rest-port conf/props)
                                        :cli-options options}))
    ;update with general info
    (swap! ui/am-status assoc :containers-mem (:mem options))
    (swap! ui/am-status assoc :containers-cores (:cores options))
    (swap! ui/am-status assoc :no-active-containers @no-containers)

    (.registerApplicationMaster rm-client "" 0 (ui/get-tracking-url))
    (log/infof "AppMaster Registered, params %s" args)
    ;start N containers

    (.addShutdownHook (Runtime/getRuntime) (Thread. #(component/stop (appmaster-system {}))))

    (dotimes [n @no-containers]
      (.addContainerRequest rm-client
                            (AMRMClient$ContainerRequest.
                              (c/make-res
                                (:mem options)
                                (:cores options))
                              nil
                              nil
                              (make-priority))))

    (while (not (= 0 @no-containers))
      (Thread/sleep (:appmaster-check-interval conf/props))
      (log/infof "Waiting for running containers (%s)" @no-containers))

    (component/stop (appmaster-system {}))
    (.unregisterApplicationMaster rm-client FinalApplicationStatus/SUCCEEDED "" "")
    )
  )
