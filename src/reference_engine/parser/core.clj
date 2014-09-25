(ns reference-engine.parser.core
  (:require [reference-engine.parser.utils :as utils]
            [reference-engine.parser.namespaces :as ns-parser]
            [reference-engine.parser.inheritance :as inheritance]
            [reference-engine.exports :as exports]))

(defn cleanup []
  (utils/cleanup-cache)
  (inheritance/cleanup-cache)
  (exports/cleanup-cache))

(defn is-normal-doclet [raw]
  (and (:name raw)
       (not (or (= (:access raw) "private")
                (= (:access raw) "protected")
                (= (:access raw) "inner")))))

(defn filter-raw-data [raw exports]
  (filter #(exports/check-exports % raw exports)
          (pmap #(assoc % :longname (utils/cleanup-name (:longname %)))
                (filter
                 (fn [meta]
                   (if (utils/force-include? meta)
                     true
                     (if (utils/ignore? meta)
                       false
                       (is-normal-doclet meta))))
                 raw))))

(defn parse [raw exports]
  (ns-parser/get-namespaces (filter-raw-data raw exports)))
