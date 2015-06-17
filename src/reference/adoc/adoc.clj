(ns reference.adoc.adoc
  (:require [reference.git :refer [run-sh]]
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

(defn- group-files [groups files]
  (partition-all groups files))

(defn- build-jsdoc [jsdoc-path files]
  (info "jsdoc run:" (concat [jsdoc-path "-X" "-a" "public"] files))
  (parse-string
   (clojure.string/replace (:out (apply sh (concat [config/jsdoc-path "-X"] files)))
                           "acgraph"
                           "anychart.graphics")
   true))

(defn- get-jsdoc [max-groups jsdoc-path path]
  (info "get-jsdoc" path)
  (let [groups (-> (get-all-files-by-ext path "adoc.js")
                   (group-files max-groups)]
    (info "groups:" (count groups))
    (apply concat (doall (pmap build-jsdoc jsdoc-path groups))))))

(defn- convert-to-jsdoc [src-path jsdoc-path]
  (info "convert-to-jsdoc" src-path jsdoc-path)
  (run-sh "rm" "-rf" jsdoc-path)
  (run-sh "cp" "-r" src-path jsdoc-path)
  (doall (map (fn [path] (sh "cp" path (str path ".js")))
              (get-all-files-by-ext jsdoc-path "adoc"))))

(defn get-doclets [data-dir max-groups jsdoc-bin version]
  (info "get-doclets" version)
  (let [src-path (str data-dir "/versions/" version)
        jsdoc-path (str data-dir "/versions-tmp/" version)]
    (convert-to-jsdoc src-path jsdoc-path)
    (filter #(and (:name %)
                  (not (or (= (:access %) "private")
                           (= (:access %) "protected")
                           (= (:access %) "inner")))
                  (not (:undocumented %))
                  (not (:inherited %))
                  (not (some (fn [tag] (= (:originalTitle tag) "ignoreDoc")) (:tags %))))
            (get-jsdoc max-groups jsdoc-bin jsdoc-path))))
