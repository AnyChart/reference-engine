(ns reference-engine.generator
  (:require [clojure.java.shell :refer [sh with-sh-dir]]
            [cheshire.core :refer [parse-string]]
            [reference-engine.parser :reref [parse-jsdoc]]
            [reference-engine.db :refer [wcar*]]
            [taoensso.carmine :as car]))

(defn get-jsdoc-info [path]
  (parse-string
   ((sh "/usr/local/bin/node" "./node_modules/jsdoc/jsdoc.js" "-r" "-X" path) :out)
   true))

(defn cleanup [project version])

(defn generate [project version])
