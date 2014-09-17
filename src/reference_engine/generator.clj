(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string]]
            [reference-engine.parser :reref [parse]]))

(defn get-jsdoc-info [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn filter-jsdoc-info [info]
  (filter (fn [info] (not (= (:access info) "private")))
          info))

(def info (reference-engine.parser/parse (get-jsdoc-info "data/acdvf/repo/src")))

(println "==============")
(println "parse results:")
(use 'clojure.pprint)
(binding [*print-right-margin* 60] (pprint info))
