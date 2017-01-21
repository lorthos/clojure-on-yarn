(ns cljyarn.yarn.tracking.ui
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [GET POST PUT DELETE ANY defroutes routes]]
            [compojure.handler :as handler]
            [cljyarn.yarn.config :as conf]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log])
  (:use org.httpkit.server)
  (:import (org.apache.hadoop.net NetUtils)))


(def am-status (atom {}))
(defn get-tracking-report []
  (json/write-str @am-status
                  :value-fn (fn [k v] (.toString v))))

(defn get-tracking-url []
  (format (:tracking-url conf/props)
          (-> (NetUtils/getHostname)
              (clojure.string/split #"/")
              first)))


;rest related
(defroutes health-routes
           (GET ["/ping"] req
             "pong")
           (GET ["/track"] req
             (get-tracking-report)))

(def app
  (->
    (routes
      health-routes)
    handler/api))

(defrecord TrackingServer [port cli-options]
  ;; Implement the Lifecycle protocol
  component/Lifecycle

  (start [component]
    (log/infof "Starting Rest server for Master Node on port: %s" port)
    ;; In the 'start' method, initialize this component
    ;; and start it running. For example, connect to a
    ;; database, create thread pools, or initialize shared
    ;; state.
    (assoc component :server (run-server app {:port port}))
    )

  (stop [component]
    (log/infof "Stopping rest server for Master Node...")
    ;; In the 'stop' method, shut down the running
    ;; component and release any external resources it has
    ;; acquired.
    (when-not (nil? (:server component))
      (:server component))
    ;; Return the component, optionally modified. Remember that if you
    ;; dissoc one of a record's base fields, you get a plain map.
    (assoc component :server nil)))

(defn make-tracking-server [port cli-options]
  (map->TrackingServer {:port port :cli-options cli-options}))


