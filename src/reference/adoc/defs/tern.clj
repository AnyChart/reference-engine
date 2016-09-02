(ns reference.adoc.defs.tern
  (:require [clojure.string :as s :refer [join]]
            [taoensso.timbre :as timbre :refer [info error]]))


(defonce ^:const p4 "    ")
(defonce ^:const p8 "        ")

;; for developing
(defonce top-level (atom nil))

(defn set-top-level! [_top-level]
  (reset! top-level _top-level))

(defn namespace-definition [top-level namespace]
  (str "declare namespace " (:full-name namespace) " {\n"

       "}"))

(defn test2 []
  (let [namespaces (join "\n\n" (map #(namespace-definition @top-level %)
                                     (sort-by :full-name (:namespaces @top-level))))]
    (spit "/media/ssd/sibental/reference-engine-data/tern/codeMirror/defs/anychart.d.json" namespaces)

    ))

(defn generate-declarations [data-dir version-key top-level]
  (info "generate TypeScript definitions")
  (let [file-name (str data-dir "/versions-static/" version-key "/anychart.json")
        namespaces (join "\n\n" (map #(namespace-definition top-level %)
                                     (sort-by :full-name (:namespaces top-level))))]
    (spit file-name namespaces)))




