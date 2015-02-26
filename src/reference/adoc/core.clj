(ns reference.adoc.core
  (:require [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.htmlgen :refer [pre-render-top-level]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.data.versions :as vdata]
            [cheshire.core :refer [generate-string]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn build [version]
  (info "build" version)
  (let [doclets (get-doclets version)
        raw-top-level (structurize doclets)
        top-level (assoc raw-top-level
                         :classes (build-inheritance (:classes raw-top-level)))
        tree-data (generate-tree top-level)
        search-index (generate-search-index top-level)]
    ;(doall (pre-render-top-level version top-level))
    (vdata/add-version version
                       (generate-string tree-data)
                       (generate-string search-index))
    (info "building" version "completed")))

(build "master")
