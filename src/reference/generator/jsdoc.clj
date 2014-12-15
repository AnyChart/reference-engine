(ns reference.generator.jsdoc
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]
            [cheshire.core :refer [parse-string]]
            [reference.config :as config]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- get-all-js-files [path]
  (let [res (map #(.getAbsolutePath %)
                 (filter #(re-matches #".*\.js" (.getName %)) (file-seq (file path))))]
    (info "get-all-js-files" path "count:" (count res))
    res))

(defn- group-files [files]
  (partition-all 8 files))

(defn- filter-jsdoc [jsdoc]
  (info "filter-jsdoc, before:" (count jsdoc))
  (let [res (filter #(and (:name %)
                          (not (or (= (:access jsdoc) "private")
                                   (= (:access jsdoc) "protected")
                                   (= (:access jsdoc) "inner"))))
                    jsdoc)]
    (info "after:" (count res))
    res))

(defn- build-jsdoc [files]
  (info "build-jsdoc" (count files))
  (info "jsdoc run:" (concat [config/node-path "./jsdoc/jsdoc.js" "-X"] files))
  (filter-jsdoc
   (parse-string
    (clojure.string/replace (:out (apply sh (concat [config/node-path "./jsdoc/jsdoc.js" "-X"] files)))
                            "acgraph"
                            "anychart.graphics")
    true)))

(defn get-jsdoc [root-path]
  (info "get-jsdoc" root-path)
  (let [groups (-> (get-all-js-files root-path) group-files)]
    (info "groups:" (count groups))
    (apply concat (doall (pmap build-jsdoc groups)))))
