(ns cljyarn.yarn.tracking.ui-test
  (:require [clojure.test :refer :all]
            [cljyarn.yarn.tracking.ui :refer :all]))

(deftest classpath-test
  (testing "classpath generation"
    (is (clojure.string/includes?
          (get-tracking-url)
          "6000/track"))))

(defn reporting-fixture [f]
  (reset! am-status {})
  (f)
  (reset! am-status {}))


(use-fixtures :once reporting-fixture)

(deftest tracing-report-test
  (testing "tracing report generation"
    (is (= "{}"
           (get-tracking-report))))
  (swap! am-status assoc :key1 "value1")
  (is (= "{\"key1\":\"value1\"}"
         (get-tracking-report)))
  )
