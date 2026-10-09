(ns reference.util.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [reference.util.utils :as utils]))

(deftest released-version?-test
  (testing "legacy branches and numbered release tags are versions"
    (doseq [k ["v7" "v8" "9.0.0" "9.1.2" "10.0.0"]]
      (is (true? (utils/released-version? k)) k)))
  (testing "v9+, old 8.x.x tags, work branches and partial versions are not"
    (doseq [k ["v9" "v10" "8.9.0" "master" "develop" "staging" "RC-8.13.0" "9.0" "9.0.0-rc1"]]
      (is (false? (utils/released-version? k)) k))))
