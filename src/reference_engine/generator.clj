(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser.core :as jsdoc-parser]
            [reference-engine.db :refer [wcar*]]
            [reference-engine.exports :refer [generate-exports]]
            [taoensso.carmine :as car]
            [clojure.java.io :refer [file]]))

(def local (atom {}))

(defn get-jsdoc-info [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn generate-local [path]
  (doall 
  (let [data (jsdoc-parser/parse (get-jsdoc-info path)
                                 (generate-exports path))]
    (println (count data))
    (swap! local (fn [d] data)))))

(defn get-local [name]
  (first (filter #(= (:full-name %) name) @local)))

(println "start generation")
(time (generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/enums.js"))

;;(time (generate-local "/Users/alex/Work/anychart/graphics/src/vector/vector.js"))


(println "hi!")
