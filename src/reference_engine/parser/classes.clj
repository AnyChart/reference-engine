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
  (utils/parse-grouped-members cdef
                               raw-data
                               function?
                               parse-function))

(defn parse-consts [cdef raw-data]
  (utils/parse-members-with-filter cdef
                                   raw-data
                                   constant?
                                   parse-constant))

(defn parse-fields [cdef raw-data]
  (utils/parse-members-with-filter cdef
                                   raw-data
                                   field?
                                   parse-field))

(defn parse-class [raw raw-data]
  (let [cdef (utils/parse-general-doclet raw)
        res (assoc cdef
              :kind "class"
              :constructor (parse-function raw)
              :inherits-names (:augments raw)
              :methods (parse-methods cdef raw-data)
              :consts (parse-consts cdef raw-data)
              :fields (parse-fields cdef raw-data))]
    (utils/cache-entry res)))
