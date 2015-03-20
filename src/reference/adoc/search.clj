(ns reference.adoc.search
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- add-simple-members [container members]
  (reduce conj #{} (map (fn [member]
                          (str (:full-name container)
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
          (add-simple-members namespace (:constants namespace))
          (add-simple-members namespace (:functions namespace))))

(defn generate-search-index [struct]
  (info "building search index")
  (concat 
   (apply concat (map build-index-from-ns (:namespaces struct)))
   (apply concat (map build-index-from-class (:classes struct)))
   (concat (map build-index-from-typedef (:typedefs struct)))
   (concat (apply concat (map build-index-from-enum (:enums struct))))))
