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
  (println "generate enum" enum-name)
  (let [enum (get-obj-from-struct (:enums struct) (:name enum-name))]
    {:name (:name enum)
     :full-name (:full-name enum)
     :kind :enum}))

(defn- generate-typedef-tree [typedef-name struct]
  (let [typedef (get-obj-from-struct (:typedefs struct) (:name typedef-name))]
    {:name (:name typedef)
     :full-name (:full-name typedef)
     :kind :typedef}))

(defn- generate-class-tree [class-name struct]
  (let [classdef (get-obj-from-struct (:classes struct) (:name class-name))]
    {:name (:name classdef)
     :full-name (:full-name classdef)
     :kind :class
     :children (sort-by (juxt :kind :name)
                        (concat
                         (simplify-methods (:methods classdef) classdef)
                         (simplify-methods (:inherited-methods classdef) classdef)
                         (map #(generate-enum-tree % struct) (:enums namespace))
                         (map #(generate-class-tree % struct) (:classes namespace))))}))

(defn- is-top-level-ns [namespace]
  (= (count (clojure.string/split (:full-name namespace) #"\."))
     0))

(defn- generate-ns-tree [namespace struct]
  {:name (:name namespace)
   :full-name (:full-name namespace)
   :kind :namespace
   :children (sort-by (juxt :kind :name)
                      (concat (simplify-members (:constants namespace) namespace :constant)
                              (simplify-members (:functions namespace) namespace :function)
                              (map #(generate-enum-tree % struct) (:enums namespace))
                              (map #(generate-typedef-tree % struct) (:typedefs namespace))
                              (map #(generate-class-tree % struct) (:classes namespace))))})

(defn- is-child-ns [ns tlns]
  (let [name (:full-name ns)
        target-name (:full-name tlns)]
    (and (re-find (re-pattern (str "^" target-name "\\.")) name)
         (not (is-top-level-ns ns))
         (not (= target-name "anychart")))))

(defn- get-parent-namespace-name [name]
  (if (.contains name ".")
    (subs name 0 (.lastIndexOf name "."))
    nil))

(defn- get-child-namespaces [ns all-nses]
  (filter #(= (get-parent-namespace-name (:full-name %)) (:full-name ns)) all-nses))

(defn- add-child-namespaces [ns all-nses]
  (let [children (get-child-namespaces ns all-nses)]
    (assoc ns :children (sort-by (juxt :kind :name)
                                 (filter #(some? (:name %))
                                         (concat (:children ns)
                                                 (map #(add-child-namespaces % all-nses) children)))))))

(defn- structurize-namespaces [all-nses]
  (let [root-namespaces (filter #(= (get-parent-namespace-name (:full-name %)) nil) all-nses)]
    (map #(add-child-namespaces % all-nses) root-namespaces)))

(defn generate-tree [struct]
  (info "generate-tree")
  (let [nses (map #(generate-ns-tree % struct) (:namespaces struct))]
    (structurize-namespaces nses)))
