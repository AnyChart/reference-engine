(ns reference.generator.versions
  (:require [taoensso.carmine :as car :refer (wcar)]
            [reference.generator.git :as git]
            [reference.generator.exports :as exports]
            [reference.config :as config]
            [reference.generator.core :refer [get-namespaces get-top-level]]
            [reference.generator.tree :as tree-gen]
            [reference.generator.generator :as html-gen]
            [cheshire.core :refer [generate-string]]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn- redis-versions-key []
  "ref-versions")

(defn- redis-version-tree-key [version]
  (str "ref-version-" version "-tree"))

(defn- redis-version-build-hash-key [version]
  (str "ref-version-" version "-hash"))

(defn force-build-version-without-git [version]
  (let [base-path (str config/data-path "versions/" version "/")
        acdvf-src (str base-path "src/")
        graphics-src (str base-path "contrib/graphics/src/")
        exports-data (exports/add-exports-from-file
                      (str graphics-src "export.js")
                      (exports/add-export-from-folder acdvf-src))
        namespaces-data (get-namespaces exports-data
                                        graphics-src
                                        acdvf-src)
        tree-data (tree-gen/generate-tree namespaces-data)]
    (doall (html-gen/pre-render-top-level version (get-top-level namespaces-data)))
    (wcar* (car/sadd (redis-versions-key) version)
           (car/set (redis-version-tree-key version) (generate-string tree-data)))))

(defn force-build-version [version]
  (if-let [branch (git/update #{version})]
    (let [base-path (str config/data-path "versions/" version "/")
          acdvf-src (str base-path "src/")
          graphics-src (str base-path "contrib/graphics/src/")
          exports-data (exports/add-exports-from-file
                        (str graphics-src "export.js")
                        (exports/add-export-from-folder acdvf-src))
          namespaces-data (get-namespaces exports-data
                                          graphics-src
                                          acdvf-src)
          tree-data (tree-gen/generate-tree namespaces-data)]
      (doall (html-gen/pre-render-top-level version (get-top-level namespaces-data)))
      (wcar* (car/sadd (redis-versions-key) version)
             (car/set (redis-version-tree-key version) (generate-string tree-data))
             (car/set (redis-version-build-hash-key version) (:commit branch)))
      (println version "- done!"))
    (println "branch " version " not found")))

(defn build [])

(force-build-version-without-git "develop")
