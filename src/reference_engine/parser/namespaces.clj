(ns reference-engine.parser.namespaces
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.typedefs :refer [typedef? parse-typedef]]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.enums :refer [enum? parse-enum]]
            [reference-engine.parser.classes :refer [js-class? parse-class]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn get-constants [ns-def raw-data]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          constant?
                                          parse-constant
                                          :constants))

(defn get-fields [ns-def raw-data]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          field?
                                          parse-field
                                          :fields))

(defn get-typedefs [ns-def raw-data top-level-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          typedef?
                                          #(parse-typedef % top-level-callback)
                                          :typedefs))

(defn get-functions [ns-def raw-data]
  (utils/parse-grouped-members-to-obj ns-def
                                      raw-data
                                      #(and (function? %)
                                            (utils/static? %))
                                      parse-function
                                      :functions))

(defn get-enums [ns-def raw-data top-level-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          enum?
                                          #(parse-enum % raw-data top-level-callback)
                                          :enums))

(defn get-classes [ns-def raw-data top-level-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          js-class?
                                          #(parse-class % raw-data top-level-callback)
                                          :classes))

(defn parse [raw raw-data top-level-callback]
  (let [ns-def (utils/parse-general-doclet raw)
        full-ns-def (merge (assoc ns-def :kind "namespace")
                           (get-constants ns-def raw-data)
                           (get-fields ns-def raw-data)
                           (get-typedefs ns-def raw-data top-level-callback)
                           (get-functions ns-def raw-data)
                           (get-enums ns-def raw-data top-level-callback)
                           (get-classes ns-def raw-data top-level-callback))]
    (top-level-callback full-ns-def)))

(defn get-namespaces [raw-data top-level-callback]
  (map #(parse % raw-data top-level-callback) (filter #(= (:kind %) "namespace") raw-data)))
