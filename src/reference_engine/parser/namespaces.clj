(ns reference-engine.parser.namespaces
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.typedefs :refer [typedef? parse-typedef]]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.enums :refer [enum? parse-enum]]
            [reference-engine.parser.classes :refer [js-class? parse-class]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn get-constants [ns-def raw-data sample-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          constant?
                                          #(parse-constant % sample-callback)
                                          :constants))

(defn get-fields [ns-def raw-data sample-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          field?
                                          #(parse-field % sample-callback)
                                          :fields))

(defn get-typedefs [ns-def raw-data top-level-callback sample-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          typedef?
                                          #(parse-typedef % top-level-callback sample-callback)
                                          :typedefs))

(defn get-functions [ns-def raw-data sample-callback]
  (utils/parse-grouped-members-to-obj ns-def
                                      raw-data
                                      #(and (function? %)
                                            (utils/static? %))
                                      #(parse-function % sample-callback)
                                      :functions))

(defn get-enums [ns-def raw-data top-level-callback sample-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          enum?
                                          #(parse-enum % raw-data top-level-callback sample-callback)
                                          :enums))

(defn get-classes [ns-def raw-data top-level-callback sample-callback]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          js-class?
                                          #(parse-class % raw-data top-level-callback sample-callback)
                                          :classes))

(defn parse [raw raw-data top-level-callback sample-callback]
  (let [ns-def (utils/parse-general-doclet raw sample-callback)
        full-ns-def (merge (assoc ns-def :kind "namespace")
                           (get-constants ns-def raw-data sample-callback)
                           (get-fields ns-def raw-data sample-callback)
                           (get-typedefs ns-def raw-data top-level-callback
                                         sample-callback)
                           (get-functions ns-def raw-data sample-callback)
                           (get-enums ns-def raw-data top-level-callback sample-callback)
                           (get-classes ns-def raw-data top-level-callback
                                        sample-callback))]
    (top-level-callback full-ns-def)))

(defn get-namespaces [raw-data top-level-callback sample-callback]
  (map #(parse % raw-data top-level-callback sample-callback)
       (filter #(= (:kind %) "namespace") raw-data)))
