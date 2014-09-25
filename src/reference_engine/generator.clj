(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser.core :as jsdoc-parser]
            [reference-engine.parser.utils :refer [counter]]
            [reference-engine.db :refer [wcar*]]
            [reference-engine.exports :refer [generate-exports] :as exports]
            [taoensso.carmine :as car]
            [clojure.java.io :refer [file]]
            [reference-engine.parser.inheritance :as inh]
            [clojure.tools.logging :as log]))

(def local (atom {}))

(defn get-jsdoc-info [path]
  (println "Getting jsdoc info")
  (time (parse-string
         ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
         true)))

(defn generate-local [path]
  (let [data (jsdoc-parser/parse (get-jsdoc-info path)
                                 (generate-exports path))]
    (println (count data))
    (swap! local (fn [d] data))))

(defn get-local [name]
  (first (filter #(= (:full-name %) name) @local)))

(println "start generation")
(inh/reset-cache)
(exports/cleanup-cache)
(time (generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src"))
;;(time (generate-local "/Users/alex/Work/anychart/graphics/src/vector/vector.js"))
(println "done!")
