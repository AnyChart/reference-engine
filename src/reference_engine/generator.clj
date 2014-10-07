(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string generate-string]]
            [reference-engine.parser.core :as jsdoc-parser]
            [reference-engine.parser.utils :as utils]
            [reference-engine.exports :refer [generate-exports] :as exports]
            [clojure.java.io :refer [file resource]]
            [reference-engine.parser.inheritance :as inh]
            [clojure.tools.logging :as log]))

(def namespaces (atom {}))
(def local-updating (atom false))
(def initial-json (atom {}))

(def running-jar 
  "Resolves the path to the current running jar file."
  (-> :keyword class (.. getProtectionDomain getCodeSource getLocation getPath)))

(defn get-jsdoc-recursive [path]
  (parse-string
   ((sh "/usr/local/bin/node" "jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn get-jsdoc [path]
  (parse-string
   ((sh "/usr/local/bin/node" "jsdoc/jsdoc.js" "-X" path) :out)
   true))

(defn get-jsdoc-info [path]
  (println "running jsdoc -x")
  (let [jsdoc-info 
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
                                  ) folders))))]
    (reset! initial-json jsdoc-info)))

(defn generate-local [path]
  (let [data (jsdoc-parser/parse (get-jsdoc-info path)
                                 (generate-exports path))]
    (println "namespaces found:" (count data))
    (reset! namespaces data)))

(defn get-local [name]
  (utils/cached-entry name))

(defn get-first-namespace []
  (:full-name (first @namespaces)))

(defn get-namespaces []
  @namespaces)

(defn init-local [path]
  (reset! local-updating true)
  (jsdoc-parser/cleanup)
  (generate-local path)
  (reset! local-updating false))

(defn update-local [path]
  (init-local path))

(defn is-updating []
  @local-updating)
