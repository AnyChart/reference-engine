(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser.core :as jsdoc-parser]
            [reference-engine.parser.utils :as utils]
            [reference-engine.exports :refer [generate-exports] :as exports]
            [clojure.java.io :refer [file resource]]
            [reference-engine.parser.inheritance :as inh]
            [reference-engine.samples :as samples]
            [clojure.tools.logging :as log]))

(def local-namespaces (atom {}))
(def local-tree (atom []))
(def local-updating (atom false))

(defn parse-jsdoc-json [json-data]
  (parse-string (clojure.string/replace json-data
                                        "acgraph"
                                        "anychart.graphics")
                true))

(defn get-jsdoc-recursive [path]
  (parse-jsdoc-json
   ((sh "/usr/local/bin/node" "jsdoc/jsdoc.js" "-r" "-X" path) :out)))

(defn get-jsdoc [path]
  (parse-jsdoc-json
   ((sh "/usr/local/bin/node" "jsdoc/jsdoc.js" "-X" path) :out)))

(defn get-jsdoc-info [path]
  (let [paths (if (.exists (file (str path "/contrib/graphics")))
                [(str path "/src") (str path "/contrib/graphics/src")]
                [(str path "/src")])]
    (println "running jsdoc -x")
    (println paths)
    (let [folders (concat
                   (filter #(and (.isDirectory %)
                                 (not (.isHidden %)))
                           (apply concat (map #(.listFiles (file %)) paths)))
                   (map file paths))
          jsdocs (doall (pmap
                         (fn [folder]
                           (let [p (.getAbsolutePath folder)]
                             (if (= p path)
                               (get-jsdoc p)
                               (get-jsdoc-recursive p))))
                         folders))]
      (apply concat jsdocs))))

(defn generate-local [path]
  (let [data (jsdoc-parser/parse (get-jsdoc-info path)
                                 (generate-exports path)
                                 (jsdoc-parser/create-cache)
                                 utils/cache-entry
                                 samples/parse-sample-local
                                 "/")]
    (println "namespaces found:" (count (:namespaces data)))
    (reset! local-tree (generate-string (:tree data)))
    (reset! local-namespaces (:namespaces data))))

(generate-local "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo")

(defn generate-for-server [project version path ns-callback top-level-callback sample-callback]
  (ns-callback (jsdoc-parser/parse (get-jsdoc-info path)
                                   (generate-exports path)
                                   (jsdoc-parser/create-cache)
                                   top-level-callback
                                   sample-callback
                                   (str "/" project "/" version "/"))))

(defn get-local [name]
  (utils/cached-entry name))

(defn get-first-namespace []
  (:full-name (first @local-namespaces)))

(defn get-namespaces []
  @local-namespaces)

(defn init-local [path]
  (reset! local-updating true)
  (jsdoc-parser/cleanup)
  (generate-local path)
  (reset! local-updating false))

(defn update-local [path]
  (init-local path))

(defn is-updating []
  @local-updating)
