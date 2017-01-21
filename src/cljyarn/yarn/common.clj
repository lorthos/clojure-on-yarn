(ns cljyarn.yarn.common
  (:import (org.apache.hadoop.yarn.util Records ConverterUtils)
           (org.apache.hadoop.yarn.api.records ContainerLaunchContext LocalResource Resource LocalResourceType LocalResourceVisibility)
           (java.util Collections HashMap Map)
           (org.apache.hadoop.yarn.api ApplicationConstants ApplicationConstants$Environment)
           (org.apache.hadoop.fs Path FileContext FileSystem)
           (org.apache.hadoop.yarn.conf YarnConfiguration)))

(defn get-hdfs-path [jar-name]
  (Path. "/" jar-name))

(declare setup-jars! setup-classpath!)
(defn set-jar!
  "Utils/YARNAPP_JAR_PATH"
  [launch-context conf jar-path jar-name]
  (let [jar (Records/newRecord LocalResource)]
    (setup-jars! conf jar-path jar)
    (.setLocalResources launch-context (Collections/singletonMap jar-name jar))))

(defn set-cp! [launch-context conf]
  (let [envi (HashMap.)]
    (setup-classpath! envi conf)
    (.setEnvironment launch-context envi)))

(defn make-launch-context [conf jar-path jar-name mem main-class]
  (let [launch-context
        (doto ^ContainerLaunchContext (Records/newRecord ContainerLaunchContext)
          (.setCommands (Collections/singletonList
                          (str
                            "$JAVA_HOME/bin/java"
                            (str " -Xmx" mem "M")
                            " "
                            main-class
                            " 1>"
                            ApplicationConstants/LOG_DIR_EXPANSION_VAR
                            "/stdout"
                            " 2>"
                            ApplicationConstants/LOG_DIR_EXPANSION_VAR
                            "/stderr")))

          )]
    (set-jar! launch-context conf jar-path jar-name)
    (set-cp! launch-context conf)
    launch-context))

(defn make-res [mem cores]
  (doto ^Resource (Records/newRecord Resource)
    (.setMemory mem)
    (.setVirtualCores cores)))

(defn setup-jars! [^YarnConfiguration conf ^Path path ^LocalResource res]
  (let [qpath (.makeQualified (FileContext/getFileContext) path)
        status (.getFileStatus (FileSystem/get conf) qpath)]

    (.setResource res (ConverterUtils/getYarnUrlFromPath qpath))
    (.setSize res (.getLen status))
    (.setTimestamp res (.getModificationTime status))
    (.setType res LocalResourceType/FILE)
    (.setVisibility res LocalResourceVisibility/PUBLIC)
    res))

(defn setup-classpath! [^Map env ^YarnConfiguration conf]
  (.put env "CLASSPATH"
        (str
          (str (.$$ ApplicationConstants$Environment/CLASSPATH)
               ApplicationConstants/CLASS_PATH_SEPARATOR
               "./*")

          (reduce str
                  (map
                    #(str ApplicationConstants/CLASS_PATH_SEPARATOR (.trim %))
                    (.getStrings conf
                                 YarnConfiguration/YARN_APPLICATION_CLASSPATH,
                                 YarnConfiguration/DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)))

          (str ApplicationConstants/CLASS_PATH_SEPARATOR "./log4j.properties")

          (when (.getBoolean conf YarnConfiguration/IS_MINI_YARN_CLUSTER false)
            (str ":" (System/getProperty "java.class.path")))))
  )

(def cli-options
  [[nil "--jar JAR_FILE" "Jar File Containing the program"
    :default "cljyarn-0.1.0-SNAPSHOT-standalone.jar"]
   [nil "--mem MEM" "Container Memory"
    :default 256
    :parse-fn #(Integer/parseInt %)]
   [nil "--cores CORES" "Container cores"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   [nil "--containers NUMBER OF CONTAINERS" "Number of Containers"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   [nil "--container-main CONTAINER MAIN" "Main class to run witnin the container"
    :default "cljyarn.kafka.container"]
   ])

(defn interleave-args [args]
  (->> (interleave args (repeat " "))
       (reduce str)
       (.trim)))