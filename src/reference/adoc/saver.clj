(ns reference.adoc.saver
  (:require [taoensso.timbre :as timbre :refer [info]]
            [reference.data.pages :as pages]))

(defn- get-namespace-data [entry]
  (assoc entry
         :has-classes (not (empty? (:classes entry)))
         :has-typedefs (not (empty? (:typedefs entry)))
         :has-enums (not (empty? (:enums entry)))
         :has-constants (not (empty? (:constants entry)))
         :has-functions (not (empty? (:functions entry)))))

(defn- get-class-data [entry]
  (assoc entry
         :has-methods (not (empty? (:methods entry)))
         :has-inherited-methods (not (empty? (:inherited-methods entry)))))

(defn- get-enum-data [entry]
  (assoc entry :has-fields (not (empty? (:fields entry)))))

(defn- get-typedef-data [entry]
  entry)

(defn save-entries [jdbc version-id version-key top-level]
  (info "save-entries" version-key (count top-level))
  
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "class"
                                (:full-name %)
                                (get-class-data %))
               (:classes top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "namespace"
                                (:full-name %)
                                (get-namespace-data %))
               (:namespaces top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "typedef"
                                (:full-name %)
                                (get-typedef-data %))
               (:typedefs top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "enum"
                                (:full-name %)
                                (get-enum-data %))
               (:enums top-level))))
