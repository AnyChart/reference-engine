(ns reference.generator.search
  (:require [taoensso.timbre :as timbre :refer [info debug]]))

(defn- add-simple-members [members]
  (debug "add-simple-members")
  (reduce conj #{} (map (fn [member]
                          (debug "add-simple-members map" (:name member))
                          (str (:member-of (first (:members member)))
                               "#"
                               (:name member)))
                        members)))

(defn- add-enum-members [enum]
  (debug "add-enum-members" (:full-name enum))
  (concat #{(:full-name enum)}
          (map #(str (:full-name enum) "#" (:name %))
               (:fields enum))))

(defn- add-typedef-members [typedef]
  (debug "add-typedef-members" (:full-name typedef))
  #{(:full-name typedef)})

(defn- add-class-members [class]
  (debug "add-class-members" (:full-name class))
  (concat #{(:full-name class)}
          (add-simple-members (:constants class))
          (add-simple-members (:static-methods class))
          (add-simple-members (:fields class))
          (add-simple-members (:static-fields class))
          (map (fn [method]
                 (:full-name (first (:members method))))
               (:methods class))))

(defn- build-index-from-ns [namespace]
  (debug "build-index-from-ns" (:full-name namespace))
  (concat #{(:full-name namespace)}
          (add-simple-members (:constants namespace))
          (add-simple-members (:fields namespace))
          (add-simple-members (:functions namespace))
          (apply concat (map add-enum-members (:enums namespace)))
          (apply concat (map add-typedef-members (:typedefs namespace)))
          (apply concat (map add-class-members (:classes namespace)))))

(defn build-index [namespaces]
  (info "building search index" (count namespaces))
  (apply concat (map build-index-from-ns namespaces)))
