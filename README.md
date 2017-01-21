# Clojure On Yarn

Runs Clojure (or other JVM) Apps on YARN.

## Running on Yarn

### Prepare the package and Upload to somewhere on hdfs
        lein uberjar
        hadoop fs -rmr /cljyarn-0.1.0-SNAPSHOT-standalone.jar
        hadoop fs -copyFromLocal target/cljyarn-0.1.0-SNAPSHOT-standalone.jar /cljyarn-0.1.0-SNAPSHOT-standalone.jar

or

        sh package.sh

### Run on Yarn

        bash submit-container.sh target/cljyarn-0.1.0-SNAPSHOT-standalone.jar --container-main=cljyarn.kafka.basic --mem=128 --cores=1 --containers=1

### Kafka Submit Example

Following script submits the jar to YARN cluster specifiying main class to run in the container along with the parameters

        bash submit-container.sh target/cljyarn-0.1.0-SNAPSHOT-standalone.jar --container-main=cljyarn.kafka.container --mem=512 --cores=1 --containers=1


params are passed to submit application -> application master -> containers

## TODO
+ emr step example


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
