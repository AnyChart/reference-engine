(ns reference-engine.parser.typedefs
  (:require [reference-engine.parser.utils :as utils]))

(defn typedef? [raw]
  (= (:kind raw) "typedef"))

(defn parse-typedef-property [raw]
  {:name (:name raw)
   :description (:description raw)
   :type (get-in raw [:type :names])})

(defn parse-typedef-properties [raw]
  {:properties (map parse-typedef-property (:properties raw))})

(defn parse-typedef-base-types [raw]
  {:type (get-in raw [:type :names])})

(defn parse-typedef [raw]
  (merge (utils/parse-general-doclet raw)
         (parse-typedef-base-types raw)
         (parse-typedef-properties raw)))
