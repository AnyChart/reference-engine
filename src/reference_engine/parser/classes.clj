(ns reference-engine.parser.classes
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn js-class? [raw]
  (= (:kind raw) "class"))

(defn parse-constructor [raw]
  {:constructor (parse-function raw)})

(defn parse-inheritance [raw]
  (if (:augments raw)
    {:inherits-names (:augments raw)}))

(defn parse-methods [cdef raw-data]
  (utils/parse-grouped-members-to-obj cdef
                                      raw-data
                                      #(and (function? %)
                                            (utils/scope-instance? %))
                                      parse-function
                                      :methods))

(defn parse-static-methods [cdef raw-data]
  (utils/parse-grouped-members-to-obj cdef
                                      raw-data
                                      #(and (function? %)
                                            (utils/static? %))
                                      parse-function
                                      :static-methods))


(defn parse-consts [cdef raw-data]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          constant?
                                          parse-constant
                                          :consts))

(defn parse-fields [cdef raw-data]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          #(and (field? %)
                                                (utils/scope-instance? %))
                                          parse-field
                                          :fields))

(defn parse-static-fields [cdef raw-data]
  (utils/parse-members-with-filter-to-obj cdef
                                          raw-data
                                          #(and (field? %)
                                                (utils/static? %))
                                          parse-field
                                          :static-fields))

(defn parse-class [raw raw-data]
  (let [cdef (utils/parse-general-doclet raw)
        res (merge (assoc cdef
                     :kind "class"
                     :constructor (parse-function raw)
                     :inherits-names (:augments raw)
                     :has-inherits-names (> (count (:augments raw)) 0))
                   (parse-methods cdef raw-data)
                   (parse-static-methods cdef raw-data)
                   (parse-consts cdef raw-data)
                   (parse-fields cdef raw-data)
                   (parse-static-fields cdef raw-data))]
    (utils/cache-entry res)))
