(ns reference-engine.parser.core
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.namespaces :as ns-parser]
            [reference-engine.parser.inheritance :as inheritance]
            [reference-engine.exports :as exports]
            [reference-engine.parser.tree :refer [create-tree]]))

(defn create-cache []
  {:inheritance (inheritance/create-cache)
   :exports (exports/create-cache)})

(defn cleanup []
  (utils/cleanup-cache))
  ;;(inheritance/cleanup-cache (:inhertantce cache))
  ;;(exports/cleanup-cache (:exports cache)))

(defn is-normal-doclet [raw]
  (and (:name raw)
       (not (or (= (:access raw) "private")
                (= (:access raw) "protected")
                (= (:access raw) "inner")))))

(defn filter-raw-data [raw exports cache]
  (filter #(or (utils/force-include? %)
               (exports/check-exports % raw exports (:exports cache) (:inheritance cache)))
          (pmap #(assoc % :longname (utils/cleanup-name (:longname %)))
                (filter
                 (fn [meta]
                   (if (utils/force-include? meta)
                     true
                     (if (utils/ignore? meta)
                       false
                       (is-normal-doclet meta))))
                 raw))))

(defn parse [raw exports cache top-level-callback sample-callback]
  (println "raw data:" (count raw))
  (let [namespaces (inheritance/inject-inherited-methods 
                    (ns-parser/get-namespaces (filter-raw-data raw exports cache)
                                              top-level-callback
                                              sample-callback)
                    (:inheritance cache)
                    top-level-callback)]
    {:namespaces namespaces
     :tree (create-tree namespaces)}))
