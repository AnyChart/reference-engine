(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser :as docs-parser]
            [reference-engine.db :refer [wcar*]]
            [reference-engine.exports :refer [filter-exports]]
            [taoensso.carmine :as car]
            [clojure.java.io :refer [file]]))

(def local (atom {}))

(defn get-jsdoc-info [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn cleanup [project version])

(defn generate [project version])

(defn generate-local [path]
  (let [filtered-data (filter-exports (get-jsdoc-info path) path)
        data (docs-parser/parse-jsdoc filtered-data)]
    (println (count filtered-data))
    (swap! local (fn [d] data))))

(defn get-local [name]
  (first (filter #(= (:full-name %) name) @local)))

(println "start generation")
(time (generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/Chart.js"))

(println "hi!")
