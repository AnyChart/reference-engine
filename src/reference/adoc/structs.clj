(ns reference.adoc.structs
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- get-doclets-by-kind [doclets kind]
  (filter #(= kind (:kind %)) doclets))

(defn- get-doclets-by-memberof-and-kind [doclets memberof kind]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %))))))

(defn- get-doclets-with-filter [doclets memberof kind filter-fn]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %))
                  (filter-fn %)))))

(defn- is-static [entry]
  (= (:scope entry) "static"))

(defn- group-functions [functions])

(defn- create-typedef [typedef doclets])

(defn- create-enum [enum doclets])

(defn- create-constant [const doclets])

(defn- create-function [func doclets])

;; class
;; - class
;; - enum
;; - function (non-static!)
(defn- create-class [class doclets]
  {:doclet class
   :enums (map #(create-enum % doclets)
               (get-doclets-with-filter doclets
                                        class
                                        "member"
                                        #(:isEnum %)))
   :classes (map #(create-class % doclets)
                 (get-doclets-by-memberof-and-kind doclets
                                                   namespace
                                                   "class"))
   :functions (group-functions (map #(create-function % doclets)
                                    (get-doclets-with-filter doclets
                                                             namespace
                                                             "function"
                                                             #(not (is-static %)))))})

;; namespace:
;; - namespace
;; - typedef
;; - enum
;; - class
;; - const
;; - static function
(defn- create-namespace [namespace doclets]
  {:doclet namespace
   :typedefs (map #(create-typedef % doclets)
                  (get-doclets-by-memberof-and-kind doclets
                                                    namespace
                                                    "typedef"))
   :enums (map #(create-enum % doclets)
               (get-doclets-with-filter doclets
                                        namespace
                                        "member"
                                        #(:isEnum %)))
   :classes (map #(create-class % doclets)
                 (get-doclets-by-memberof-and-kind doclets
                                                   namespace
                                                   "class"))
   :constants (map #(create-constant % doclets)
                   (get-doclets-with-filter doclets
                                            namespace
                                            "member"
                                            is-static))
   :functions (map #(create-function % doclets)
                   (get-doclets-with-filter doclets
                                            namespace
                                            "function"
                                            is-static))})

(defn structurize [doclets]
  (info "structurize")
  (let [namespaces (get-doclets-by-kind doclets "namespace")]
    ))
