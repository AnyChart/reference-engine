(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser.core :as jsdoc-parser]
            [reference-engine.parser.utils :as utils]
            [reference-engine.exports :refer [generate-exports] :as exports]
            [clojure.java.io :refer [file]]
            [reference-engine.parser.inheritance :as inh]
            [clojure.tools.logging :as log]))

(def namespaces (atom {}))

(defn get-jsdoc-recursive [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn get-jsdoc [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-X" path) :out)
   true))

(defn get-jsdoc-info [path]
  (println "running jsdoc -x")
  (time
   (let [folders (conj
                  (filter #(and (.isDirectory %)
                                (not (.isHidden %)))
                          (vec (.listFiles (file path))))
                  (file path))]
     (apply concat (doall (pmap
                           (fn [folder]
                             (let [p (.getAbsolutePath folder)]
                               (if (= p path)
                                 (get-jsdoc p)
                                 (get-jsdoc-recursive p)))
                             ) folders))))))

(defn generate-local [path]
  (let [data (jsdoc-parser/parse (get-jsdoc-info path)
                                 (generate-exports path))]
    (println "result:" (count data))
    (reset! namespaces data)))

(defn get-local [name]
  (utils/cached-entry name))

(defn get-namespaces []
  @namespaces)

(println "start generation")
(jsdoc-parser/cleanup)

;;(time (generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src"))
(generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src")
;;(time (generate-local "/Users/alex/Work/anychart/graphics/src/vector/vector.js"))
(println "done!")
