(ns reference-engine.parser.classes
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn js-class? [raw]
  (= (:kind raw) "class"))

(defn parse-constructor [raw sample-callback]
  {:constructor (parse-function raw sample-callback)})

(defn parse-inheritance [raw]
  (if (:augments raw)
    {:inherits-names (:augments raw)}))

(defn parse-methods [cdef raw-data sample-callback]
  (utils/parse-grouped-members-to-obj cdef
                                      raw-data
                                      #(and (function? %)
                                            (utils/scope-instance? %))
                                      #(parse-function % sample-callback)
                                      :methods))

(defn parse-static-methods [cdef raw-data sample-callback]
  (utils/parse-grouped-members-to-obj cdef
                                      raw-data
                                      #(and (function? %)
                                            (utils/static? %))
                                      #(parse-function sample-callback)
                                      :static-methods))


(defn parse-consts [cdef raw-data sample-callback]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          constant?
                                          #(parse-constant % sample-callback)
                                          :consts))

(defn parse-fields [cdef raw-data sample-callback]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          #(and (field? %)
                                                (utils/scope-instance? %))
                                          #(parse-field % sample-callback)
                                          :fields))

(defn parse-static-fields [cdef raw-data sample-callback]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          #(and (field? %)
                                                (utils/static? %))
                                          #(parse-field % sample-callback)
                                          :static-fields))

(defn parse-class [raw raw-data top-level-callback sample-callback]
  (let [cdef (utils/parse-general-doclet raw sample-callback)
        res (merge (assoc cdef
                     :kind "class"
                     :constructor (parse-function raw sample-callback)
                     :inherits-names (:augments raw)
                     :has-inherits-names (> (count (:augments raw)) 0))
                   (parse-methods cdef raw-data sample-callback)
                   (parse-static-methods cdef raw-data sample-callback)
                   (parse-consts cdef raw-data sample-callback)
                   (parse-fields cdef raw-data sample-callback)
                   (parse-static-fields cdef raw-data sample-callback))]
    (top-level-callback res)))
