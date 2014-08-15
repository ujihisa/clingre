(ns clingre.core-test
  (:require [clojure.test :refer :all]
            [clingre.core :refer :all]))

(deftest session-create-test
  (testing "session-create"
    (let [session (session-create)]
      (is (instance? String session)))))

(deftest say-test
  (testing "say"
    (let [session (session-create)
          say-result (say session "mcujm" "test")]
      (is say-result))))

#_ (clojure.test/run-tests)
