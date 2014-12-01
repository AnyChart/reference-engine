(ns reference.generator.versions
  (:require [taoensso.carmine :as car :refer (wcar)]
            [reference.generator.git :as git]
            [cheshire.core :refer [generate-string]]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn- redis-versions-key []
  "ref-versions")

(defn- redis-version-tree-key [version]
  (str "ref-version-" version "-tree"))

(defn- redis-version-build-hash-key [version]
  (str "ref-version-" version "-hash"))

(defn force-build-version [version]
  (if-let [branch (git/update #{version})]
    (let [a 1]
      (wcar* (car/sadd (redis-versions-key) version)
             (car/set (redis-version-build-hash-key version) (:commit branch))))
    (println "branch " version " not found")))

(defn build [])

(force-build-version "develop")
