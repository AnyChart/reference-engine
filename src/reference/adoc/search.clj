(ns reference.adoc.search
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- generate-entry-search-index [entry]
  {:full-name (:full-name entry)
   :name (:name entry)})

(defn- generate-enum-search-index [enum]
  (assoc (generate-entry-search-index enum)
         :fields (map :name (:fields enum))))

(defn- generate-typedef-search-index [typedef]
  (assoc (generate-entry-search-index typedef)
         :properties (map :name (:properties typedef))))

(defn- generate-class-search-index [class]
  (assoc (generate-entry-search-index class)
         :methods (map :name (:methods class))))

(defn- generate-namespace-search-index [namespace]
  (assoc (generate-entry-search-index namespace)
         :functions (map :name (:functions namespace))
         :constants (map :name (:constants namespace))))

(defn generate-search-index [top-level]
  {:enums (map generate-enum-search-index (:enums top-level))
   :typedefs (map generate-typedef-search-index (:typedefs top-level))
   :classes (map generate-class-search-index (:classes top-level))
   :namespaces (map generate-namespace-search-index (:namespaces top-level))})
