(ns reference.generator.jsdoc
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]
            [cheshire.core :refer [parse-string]]
            [reference.config :as config]))

(defn- get-all-js-files [path]
  (map #(.getAbsolutePath %)
       (filter #(re-matches #".*\.js" (.getName %)) (file-seq (file path)))))

(defn- group-files [files]
  (partition-all 8 files))

(defn- filter-jsdoc [jsdoc]
  (filter #(and (:name %)
                (not (or (= (:access jsdoc) "private")
                         (= (:access jsdoc) "protected")
                         (= (:access jsdoc) "inner"))))
          jsdoc))

(defn- build-jsdoc [files]
  (filter-jsdoc
   (parse-string
    (:out (apply sh (concat ["node" "./jsdoc/jsdoc.js" "-X"] files)))
    true)))

(defn get-jsdoc [root-path]
  (let [groups (-> (get-all-js-files root-path) group-files)]
    (apply concat (pmap build-jsdoc groups))))
