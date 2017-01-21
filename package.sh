#!/usr/bin/env bash

lein uberjar
hadoop fs -rmr /cljyarn-0.1.0-SNAPSHOT-standalone.jar
hadoop fs -copyFromLocal target/cljyarn-0.1.0-SNAPSHOT-standalone.jar /cljyarn-0.1.0-SNAPSHOT-standalone.jar


