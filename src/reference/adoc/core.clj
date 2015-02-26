(ns reference.adoc.core
  (:require [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.htmlgen :refer [prerender-top-level]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn build [version]
  (info "build" version)
  (let [doclets (get-doclets version)
        raw-top-level (structurize doclets)
        top-level (assoc raw-top-level
                         :classes (build-inheritance (:classes raw-top-level)))]
    (info "version" version)
    (info "TL" top-level)
    (prerender-top-level version top-level)))

(build "master")
