(ns reference.generator.generator
  (:require [clostache.parser :refer [render-resource]]
            [reference.config :as config]
            [clojure.java.io :refer [resource]]
            [clojure.java.shell :refer [sh]]))

(defn- render-template [version template entry]
  (render-resource template
                   {:main entry
                    :link #(str "/" version "/" %)}
                   {:fn-part (slurp (resource "templates/fn.mustache"))
                    :examples (slurp (resource "templates/example.mustache"))}))

(defn- render-namespace [version entry]
  (render-template version
                   "templates/ns.mustache"
                   (assoc entry
                     :has-classes (not (empty? (:classes entry)))
                     :has-typedefs (not (empty? (:typedefs entry)))
                     :has-enums (not (empty? (:enums entry)))
                     :has-constants (not (empty? (:constants entry)))
                     :has-functions (not (empty? (:functions entry))))))

(defn- render-class [version entry]
  (render-template version
                   "templates/class.mustache"
                   (assoc entry
                     :has-inherits-names (not (empty? (:inherits entry)))
                     :inherits-names (:inherits entry)
                     :has-constants (not (empty? (:constants entry)))
                     :has-static-methods (not (empty? (:static-methods entry)))
                     :has-methods (not (empty? (:methods entry)))
                     :has-fields (not (empty? (:fields entry)))
                     :has-static-fields (not (empty? (:static-fields entry))))))

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
  (sh "rm" "-rf" (str config/data-path "versions-data/" version))
  (sh "mkdir" (str config/data-path "versions-data/" version))
  (pmap #(save version %) top-level))
