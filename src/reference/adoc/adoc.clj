(ns reference.adoc.adoc
  (:require [reference.git :refer [run-sh]]
            [cheshire.core :as json]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]
            [taoensso.timbre :as timbre :refer [info]]
            [clojure.string :as string]))


(defn- get-all-files-by-ext [path extension]
  (let [res (doall (map #(.getAbsolutePath %)
                        (filter #(and
                                   (re-matches (re-pattern (str ".*\\." extension)) (.getName %))
                                   (not (.isHidden %)))
                                (file-seq (file path)))))]
    (info "get-all-js-files" path "count:" (count res))
    res))


(defn- group-files [groups files]
  (doall (partition-all (/ (count files) groups) files)))


(defn- build-jsdoc [jsdoc-path version files]
  (when (seq files)
    (info "jsdoc run:" (vec (concat [jsdoc-path "-X" "-a" "public"])))
    (let [full-res (apply sh (vec (concat [jsdoc-path "-X"] (vec files))))
          res (:out full-res)]
      (info "got res" (count res) (:err full-res))
      (json/parse-string
        (-> res
            (string/replace "acgraph" "anychart.graphics")
            (string/replace "{{branch-name}}" version))
        true))))


(defn- get-jsdoc [max-groups jsdoc-path path version]
  (info "get-jsdoc" path max-groups jsdoc-path)
  (let [groups (group-files max-groups (get-all-files-by-ext path "adoc.js"))]
    (info "groups:" (count groups))
    (apply concat (doall (pmap #(build-jsdoc jsdoc-path version %) groups)))))


(defn- convert-to-jsdoc [src-path jsdoc-path]
  (info "convert-to-jsdoc" src-path jsdoc-path)
  (run-sh "rm" "-rf" jsdoc-path)
  (run-sh "cp" "-r" src-path jsdoc-path)
  (doall (map (fn [path] (sh "cp" path (str path ".js")))
              (get-all-files-by-ext jsdoc-path "adoc"))))


(defn get-all-doclets [data-dir max-groups jsdoc-bin version]
  (info "get-doclets" version jsdoc-bin)
  (let [src-path (str data-dir "/versions/" version)
        jsdoc-path (str data-dir "/versions-tmp/" version)]
    (convert-to-jsdoc src-path jsdoc-path)
    (let [jsdocs (get-jsdoc max-groups jsdoc-bin jsdoc-path version)
          doclets (filter #(and (:name %)
                                (not (or (= (:access %) "private")
                                         (= (:access %) "protected")
                                         (= (:access %) "inner")))
                                (not (:undocumented %))
                                (not (:inherited %))) jsdocs)]
      doclets)))


(defn- ignored? [doclet]
  (some (fn [tag] (= (:originalTitle tag) "ignoreDoc")) (:tags doclet)))


(defn get-not-ignored-doclets [doclets]
  (filter #(not (ignored? %)) doclets))