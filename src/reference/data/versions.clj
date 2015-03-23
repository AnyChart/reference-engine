(ns reference.data.versions
  (:require [taoensso.carmine :as car :refer (wcar)]
            [version-clj.core :refer [version-compare]]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn- redis-versions-key []
  "ref-versions")

(defn- redis-version-tree-key [version]
  (str "ref-version-" version "-tree"))

(defn- redis-version-search-key [version]
  (str "ref-version-" version "-search"))

(defn- redis-version-build-hash-key [version]
  (str "ref-version-" version "-hash"))

(defn add-version [version tree-data search-data]
  (wcar* (car/sadd (redis-versions-key) version)
         (car/set (redis-version-tree-key version) tree-data)
         (car/set (redis-version-search-key version) search-data)))

(defn remove-version [version]
  (wcar* (car/srem (redis-versions-key) version)
         (car/del (redis-version-tree-key version))
         (car/del (redis-version-search-key version))))

(defn update-hash [version hash]
  (wcar* (car/set (redis-version-build-hash-key version) hash)))

(defn get-hash [version]
  (wcar* (car/get (redis-version-build-hash-key version))))

(defn version-exists? [version]
  (= 1 (wcar* (car/sismember (redis-versions-key) version))))

(defn default-version []
  (first (reverse (sort version-compare (wcar* (car/smembers (redis-versions-key)))))))

(defn all-versions []
  (wcar* (car/smembers (redis-versions-key))))

(defn tree-json [version]
  (wcar* (car/get (redis-version-tree-key version))))

(defn search-index [version]
  (wcar* (car/get (redis-version-search-key version))))
