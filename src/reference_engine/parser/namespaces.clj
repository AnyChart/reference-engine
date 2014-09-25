(ns reference-engine.parser.namespaces
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.constants :refer [constant? parse-constant]]
            [reference-engine.parser.typedefs :refer [typedef? parse-typedef]]
            [reference-engine.parser.functions :refer [function? parse-function]]
            [reference-engine.parser.enums :refer [enum? parse-enum]]
            [reference-engine.parser.classes :refer [js-class? parse-class]]
            [reference-engine.parser.fields :refer [field? parse-field]]))

(defn parse [raw raw-data]
  (let [ns-def (utils/parse-general-doclet raw)]
    (assoc ns-def
      :constants (utils/parse-members-with-filter ns-def
                                                  raw-data
                                                  constant?
                                                  parse-constant)
      :fields (utils/parse-members-with-filter ns-def
                                               raw-data
                                               field?
                                               parse-field)
      :typedefs (utils/parse-members-with-filter ns-def
                                                 raw-data
                                                 typedef?
                                                 parse-typedef)
      :functions (utils/parse-grouped-members ns-def
                                              raw-data
                                              #(and (function? %)
                                                    (utils/static? %))
                                              parse-function)
      :enums (utils/parse-members-with-filter ns-def
                                              raw-data
                                              enum?
                                              #(parse-enum % raw-data))
      :classes (utils/parse-members-with-filter ns-def
                                                raw-data
                                                js-class?
                                                #(parse-class % raw-data)))))

(defn get-namespaces [raw-data]
  (println "data to parse:")
  (println (count raw-data))
  (map #(parse % raw-data) (filter #(= (:kind %) "namespace") raw-data)))
