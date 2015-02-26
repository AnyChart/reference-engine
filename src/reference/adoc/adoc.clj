(ns reference.adoc.adoc
  (:require [reference.config :as config]
            [reference.adoc.structs :refer [structurize get-all-classes]]
            [reference.generator.git :refer [run-sh]]
            [cheshire.core :refer [parse-string]]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- get-all-files-by-ext [path extension]
  (let [res (map #(.getAbsolutePath %)
                 (filter #(and
                           (re-matches (re-pattern (str ".*\\." extension)) (.getName %))
                           (not (.isHidden %)))
                         (file-seq (file path))))]
    (info "get-all-js-files" path "count:" (count res))
    res))

(defn- group-files [files]
  (partition-all config/jsdoc-numproc files))

(defn- build-jsdoc [files]
  (info "jsdoc run:" (concat [config/jsdoc-path "-X" "-a" "public"] files))
  (parse-string
   (clojure.string/replace (:out (apply sh (concat [config/jsdoc-path "-X"] files)))
                           "acgraph"
                           "anychart.graphics")
   true))

(defn- get-jsdoc [path]
  (info "get-jsdoc" path)
  (let [groups (-> (get-all-files-by-ext path "js") group-files)]
    (info "groups:" (count groups))
    (apply concat (doall (pmap build-jsdoc groups)))))

(defn- convert-to-jsdoc [src-path jsdoc-path]
  (info "convert-to-jsdoc" src-path jsdoc-path)
  (run-sh "rm" "-rf" jsdoc-path)
  (run-sh "cp" "-r" src-path jsdoc-path)
  (doall (map (fn [path] (sh "cp" path (str path ".js")))
              (get-all-files-by-ext jsdoc-path "adoc"))))

(defn build [version]
  (info "build" version)
  (let [src-path (str config/versions-path version)
        jsdoc-path (str config/adoc-tmp-path version)]
    (convert-to-jsdoc src-path jsdoc-path)
    (let [doclets (get-jsdoc jsdoc-path)
          top-level (structurize doclets)]
      (info "doclets count" (count doclets))
      (info "top level entries" (count top-level)))))
