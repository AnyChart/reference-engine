(ns reference.config
  (:require [clojure.java.io :refer [file]]))

(def domain (atom "localhost"))
(defn set-domain-from-request [request]
  (reset! domain (:server-name request)))

(defn is-prod []
  (.endsWith @domain ".com"))

(defn playground-domain []
  (if (is-prod)
    "playground.anychart.com"
    "playground.anychart.stg"))

(defn reference-domain []
  (if (is-prod)
    "api.anychart.com"
    "api.anychart.stg"))

(defn docs-domain []
  (if (is-prod)
    "docs.anychart.com"
    "docs.anychart.stg"))

(defn filter-branch [name]
  (println "checking" name)
  (if (is-prod)
    (and (not (.contains name "->"))
         (re-matches #"\d\.\d\.\d" name))
    true))

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

