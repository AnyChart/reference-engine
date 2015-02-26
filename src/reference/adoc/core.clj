(ns reference.adoc.core
  (:require [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn build [version]
  (info "build" version)
  (let [doclets (get-doclets version)
        raw-top-level (structurize doclets)
        top-level (assoc raw-top-level
                         :classes (build-inheritance (:classes raw-top-level)))]
    ))
