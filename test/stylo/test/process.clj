(ns stylo.test.process
  (:use [stylo.process])
  (:use [clojure.test]))

(deftest test-convert-line []
  (let [line "This is the line we need to count. All of the words should count."
        seq-line ["this" "is" "the" "line" "we" "need" "to" "count" "all" "of" "the" "words" "should" "count"]]
    (is (= seq-line (convert-line-to-words line))))) 

(deftest test-line-count []
  (let [line "This is the line we need to count. All of the words should count."
        response {"this" 1, "is" 1, "the" 2, "line" 1, "we" 1, "need" 1, "to" 1, "count" 2, "all" 1, "of" 1, "words" 1, "should" 1}]
    (is (= response (count-words line)))))
