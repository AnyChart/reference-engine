(ns reference.adoc.tree-minimized
  (:require [taoensso.timbre :as timbre]))

(defn- get-obj-from-struct [struct name]
  (first (filter #(= (:full-name %) name) struct)))


(defn- simplify-members [members container kind]
  (map (fn [member]
         {:name      (:name member)
          :full-name (str (:full-name container) "#" (:name member))
          :kind      kind})
       members))


(defn- to-enum [enum]
  {:name      (:name enum)
   :full-name (:full-name enum)
   :kind      :enum})


(defn- generate-enum-tree [enum-name struct]
  (let [enum (get-obj-from-struct (:enums struct) (:name enum-name))]
    (to-enum enum)))


(defn- to-typedef [typedef]
  {:name      (:name typedef)
   :full-name (:full-name typedef)
   :kind      :typedef})


(defn- generate-typedef-tree [typedef-name struct]
  (let [typedef (get-obj-from-struct (:typedefs struct) (:name typedef-name))]
    (to-typedef typedef)))


(defn class-typedefs [struct class]
  (let [typedefs (filter (fn [typedef]
                           (.startsWith (:full-name typedef)
                                        (str (:full-name class) ".")))
                         (:typedefs struct))]
    typedefs))


(defn class-enums [struct class]
  (let [enums (filter (fn [enum]
                        (.startsWith (:full-name enum)
                                     (str (:full-name class) ".")))
                      (:enums struct))]
    enums))


(defn- generate-class-tree [class-name struct]
  (let [classdef (get-obj-from-struct (:classes struct) (:name class-name))]
    {:name      (:name classdef)
     :full-name (:full-name classdef)
     :kind      :class
     :methods   (sort (map :name
                           (concat
                             (simplify-members (:methods classdef) classdef :method)
                             (simplify-members (:inherited-methods classdef) classdef :method))))
     :enums     (sort (map :name (class-enums struct classdef)))
     :typedefs  (sort (map :name (class-typedefs struct classdef)))}))


(defn- generate-ns-tree [namespace struct]
  {:name      (:name namespace)
   :full-name (:full-name namespace)
   :kind      :namespace
   :constants (sort (map :name (:constants namespace)))
   :functions (sort (map :name (:functions namespace)))
   :enums     (sort (map :name (map #(generate-enum-tree % struct) (:enums namespace))))
   :typedefs  (sort (map :name (map #(generate-typedef-tree % struct) (:typedefs namespace))))
   :classes   (sort-by :name (map #(generate-class-tree % struct) (:classes namespace)))})


(defn- get-parent-namespace-name [name]
  (when (.contains name ".")
    (subs name 0 (.lastIndexOf name "."))))


(defn- get-child-namespaces [ns all-nses]
  (filter #(= (get-parent-namespace-name (:full-name %)) (:full-name ns)) all-nses))


(defn- add-child-namespaces [ns all-nses]
  (let [children (get-child-namespaces ns all-nses)]
    (assoc ns :namespaces (sort-by :name
                                   (filter #(some? (:name %))
                                           (map #(add-child-namespaces % all-nses) children))))))


(defn- structurize-namespaces [all-nses]
  (let [root-namespaces (filter #(nil? (get-parent-namespace-name (:full-name %))) all-nses)]
    (map #(add-child-namespaces % all-nses) root-namespaces)))


(defn generate-tree [struct]
  (timbre/info "generate-tree min")
  (let [nses (map #(generate-ns-tree % struct) (:namespaces struct))]
    (structurize-namespaces nses)))

