(ns reference-engine.parser.namespaces
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.typedefs :refer [typedef? parse-typedef]]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.enums :refer [enum? parse-enum]]
            [reference-engine.parser.classes :refer [js-class? parse-class]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn get-constants [ns-def raw-data sample-callback links-prefix]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          constant?
                                          #(parse-constant % sample-callback links-prefix)
                                          :constants))

(defn get-fields [ns-def raw-data sample-callback links-prefix]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          field?
                                          #(parse-field % sample-callback links-prefix)
                                          :fields))

(defn get-typedefs [ns-def raw-data top-level-callback sample-callback links-prefix]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          typedef?
                                          #(parse-typedef %
                                                          top-level-callback
                                                          sample-callback
                                                          links-prefix)
                                          :typedefs))

(defn get-functions [ns-def raw-data sample-callback links-prefix]
  (utils/parse-grouped-members-to-obj ns-def
                                      raw-data
                                      #(and (function? %)
                                            (utils/static? %))
                                      #(parse-function % sample-callback links-prefix)
                                      :functions))

(defn get-enums [ns-def raw-data top-level-callback sample-callback links-prefix]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          enum?
                                          #(parse-enum % raw-data
                                                       top-level-callback
                                                       sample-callback
                                                       links-prefix)
                                          :enums))

(defn get-classes [ns-def raw-data top-level-callback sample-callback links-prefix]
  (utils/parse-members-with-filter-to-obj ns-def
                                          raw-data
                                          js-class?
                                          #(parse-class % raw-data
                                                        top-level-callback
                                                        sample-callback
                                                        links-prefix)
                                          :classes))

(defn parse [raw raw-data top-level-callback sample-callback links-prefix]
  (let [ns-def (utils/parse-general-doclet raw sample-callback links-prefix)
        full-ns-def (merge (assoc ns-def :kind "namespace")
                           (get-constants ns-def raw-data sample-callback links-prefix)
                           (get-fields ns-def raw-data sample-callback links-prefix)
                           (get-typedefs ns-def raw-data top-level-callback
                                         sample-callback links-prefix)
                           (get-functions ns-def raw-data sample-callback links-prefix)
                           (get-enums ns-def raw-data top-level-callback sample-callback links-prefix)
                           (get-classes ns-def raw-data top-level-callback
                                        sample-callback links-prefix))]
    (top-level-callback full-ns-def)))

(defn get-namespaces [raw-data top-level-callback sample-callback links-prefix]
  (map #(parse % raw-data top-level-callback sample-callback links-prefix)
       (filter #(= (:kind %) "namespace") raw-data)))
