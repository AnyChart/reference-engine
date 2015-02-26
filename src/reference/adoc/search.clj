(ns reference.adoc.search
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- add-simple-members [members]
  (reduce conj #{} (map (fn [member]
                          (str (:member-of (first (:members member)))
                               "#"
                               (:name member)))
                        members)))

(defn- add-methods [class methods]
  (map #(str (:full-name class) "#" (:name %)) methods))

(defn- build-index-from-class [class]
  (concat #{(:full-name class)}
          (add-methods class (:methods class))
          (add-methods class (:inherited-methods class))))

(defn- build-index-from-typedef [typedef]
  (:full-name typedef))

(defn- build-index-from-enum [enum]
  (concat #{(:full-name enum)}
          (map #(str (:full-name enum) "#" (:name %))
               (:fields enum))))

(defn- build-index-from-ns [namespace]
  (concat #{(:full-name namespace)}
          (add-simple-members (:constants namespace))
          (add-simple-members (:functions namespace))))

(defn generate-search-index [struct]
  (info "building search index")
  (concat (map build-index-from-ns (:namespaces struct))
          (map build-index-from-class (:classes struct))
          (map build-index-from-typedef (:typedefs struct))
          (map build-index-from-enum (:enums struct))))
