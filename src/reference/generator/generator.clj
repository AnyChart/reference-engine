(ns reference.generator.generator
  (:require [clostache.parser :refer [render-resource]]
            [reference.config :as config]
            [clojure.java.io :refer [resource]]))

(defn- render-template [version template entry]
  (render-resource template
                   {:main entry
                    :link #(str "/" version "/")}
                   {:fn-part (slurp (resource "templates/fn.mustache"))
                    :examples (slurp (resource "templates/example.mustache"))}))

(defn- render-namespace [version entry]
  (render-template version
                   "templates/ns.mustache"
                   entry))

(defn- render-class [version entry]
  (render-template version
                   "templates/class.mustache"
                   entry))

(defn- render-enum [version entry]
  (render-template version
                   "templates/enum.mustache"
                   entry))

(defn- render-typedef [version entry]
  (render-template version
                   "templates/typedef.mustache"
                   entry))

(defn- render-top-level [version entry]
  (case (:kind entry)
    "namespace" (render-namespace version entry)
    "class" (render-class version entry)
    "enum" (render-enum version entry)
    "typedef" (render-typedef version entry)
    nil))

(defn- save [version entry]
  (let [path (str config/data-path "/versions-data/" version "/" (:full-name entry) ".html")
        data (render-top-level version entry)]
    (if data
      (spit path data))))

(defn pre-render-top-level [version top-level]
  (pmap #(save version %) top-level))
