(ns reference.generator.versions
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn- redis-versions-key []
  "ref-versions")

(defn- redis-version-tree-key [version]
  (str "ref-version-" version "-tree"))

(defn- redis-version-build-hash-key [version]
  (str "ref-version-" version "-hash"))


(defn build [])
