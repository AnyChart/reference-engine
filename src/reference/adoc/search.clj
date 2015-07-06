(ns reference.adoc.search
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- generate-entry-search-index [entry]
  {:full-name (:full-name entry)
   :link (:full-name entry)
   :name (:name entry)})

(defn- generate-function-search-index [fn ns-name]
  {:full-name (:full-name fn)
   :link (str ns-name "#" (:name fn))
   :name (:name fn)})

(defn- generate-method-search-index [method class-name]
  {:full-name (str class-name "." (:name method))
   :link (str class-name "#" (:name method))
   :name (:name method)})

(defn- generate-enum-search-index [enum]
  (generate-entry-search-index enum))

(defn- generate-typedef-search-index [typedef]
  (generate-entry-search-index typedef))

(defn- generate-class-search-index [class]
  (assoc (generate-entry-search-index class)
         :methods (map #(generate-method-search-index % (:full-name class))
                       (:methods class))))

(defn- generate-namespace-search-index [namespace]
  (assoc (generate-entry-search-index namespace)
         :functions (map #(generate-function-search-index % (:full-name namespace))
                         (:functions namespace))
         :constants (map generate-entry-search-index (:constants namespace))))

(defn generate-search-index [top-level]
  {:enums (map generate-enum-search-index (:enums top-level))
   :typedefs (map generate-typedef-search-index (:typedefs top-level))
   :classes (map generate-class-search-index (:classes top-level))
   :namespaces (map generate-namespace-search-index (:namespaces top-level))})
