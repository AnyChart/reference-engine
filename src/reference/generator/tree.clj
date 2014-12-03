(ns reference.generator.tree
  (:require [clojure.pprint :refer [pprint]]))

(defn- simplify-members [members kind]
  (if (seq members)
    (map (fn [member]
           {:name member
            :kind kind})
         (set (map :name members)))))

(defn- simplify-methods [methods]
  (map (fn [method]
         {:name (:name method)
          :kind :method})
       methods))

(defn- generate-enum-tree [enum]
  {:name (:name enum)
   :kind :enum
   :children (if (seq? (:fields enum))
               (map (fn [field]
                      {:name (:name field)
                       :kind :enum-member})
                    (:fields enum))
               [])})

(defn- generate-typedef-tree [typedef]
  {:name (:name typedef)
   :kind :typedef})

(defn- generate-class-tree [classdef]
  {:name (:name classdef)
   :kind :class
   :children (sort-by :name
                      (concat (simplify-members (:constants classdef) :constant)
                              (simplify-methods (:methods classdef))
                              (simplify-members (:static-methods classdef) :static-methods)
                              (simplify-members (:fields classdef) :field)
                              (simplify-members (:static-fields classdef) :static-field)))})

(defn- generate-ns-tree [namespace]
  {:name (:full-name namespace)
   :kind :namespace
   :children (sort-by :name
                      (concat (simplify-members (:constants namespace) :constant)
                              (simplify-members (:fields namespace) :field)
                              (simplify-members (:functions namespace) :function)
                              (map generate-enum-tree (:enums namespace))
                              (map generate-typedef-tree (:typedefs namespace))
                              (map generate-class-tree (:classes namespace))))})

(defn generate-tree [namespaces]
  (map generate-ns-tree (sort-by :full-name namespaces)))
