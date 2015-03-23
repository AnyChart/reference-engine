(ns reference.config
  (:require [clojure.java.io :refer [file]]))

(def base-path (if (not (System/getProperty "dev"))
                 "/apps/reference/"
                 "/Users/alex/Work/anychart/reference-engine/"))
(def data-path (str base-path "data/"))
(def keys-path (str base-path "keys/"))
(def node-path (if (not (System/getProperty "dev"))
                 "/usr/bin/nodejs"
                 "node"))
(def adoc-tmp-path (str data-path "tmp-adoc/"))
(def versions-path (str data-path "versions/"))
(def samples-path (str data-path "samples/"))
(def static-path (str data-path "static/"))

(def jsdoc-path "/usr/local/bin/jsdoc")
(def jsdoc-numproc 8)

