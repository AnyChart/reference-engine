(ns reference.adoc.tree
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- get-obj-from-struct [struct name]
  (first (filter #(= (:full-name %) name) struct)))

(defn- simplify-members [members container kind]
  (map (fn [member]
         {:name (:name member)
          :full-name (str (:full-name container) "#" (:name member))
          :kind kind})
       members))

(defn- simplify-methods [methods container]
  (map (fn [method]
         {:name (:name method)
          :full-name (str (:full-name container) "#" (:name method))
          :kind :method})
       methods))

(defn- generate-enum-tree [enum-name struct]
  (let [enum (get-obj-from-struct (:enums struct) enum-name)]
    {:name (:name enum)
     :full-name (:full-name enum)
     :kind :enum}))

(defn- generate-typedef-tree [typedef-name struct]
  (let [typedef (get-obj-from-struct (:typedefs struct) typedef-name)]
    {:name (:name typedef)
     :full-name (:full-name typedef)
     :kind :typedef}))

(defn- generate-class-tree [class-name struct]
  (info "class tree")
  (let [classdef (get-obj-from-struct (:classes struct) class-name)]
    {:name (:name classdef)
     :full-name (:full-name classdef)
     :kind :class
     :children (sort-by :name
                        (concat
                         (simplify-methods (:methods classdef) classdef)
                         (simplify-methods (:inherited-methods classdef) classdef)
                         (map #(generate-enum-tree % struct) (:enums namespace))
                         (map #(generate-class-tree % struct) (:classes namespace))))}))

(defn- generate-ns-tree [namespace struct]
  {:name (:full-name namespace)
   :full-name (:full-name namespace)
   :kind :namespace
   :children (sort-by :name
                      (concat (simplify-members (:constants namespace) namespace :constant)
                              (simplify-members (:functions namespace) namespace :function)
                              (map #(generate-enum-tree % struct) (:enums namespace))
                              (map #(generate-typedef-tree % struct) (:typedefs namespace))
                              (map #(generate-class-tree % struct) (:classes namespace))))})

(defn generate-tree [struct]
  (info "generate-tree")
  (map #(generate-ns-tree % struct) (sort-by :full-name (:namespaces struct))))
