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

(def is-prod (boolean (System/getProperty "prod")))

(println "production mode:" is-prod)
