(ns reference-engine.parser.typedefs
  (:require [reference-engine.parser.utils :as utils]))

(defn typedef? [raw]
  (= (:kind raw) "typedef"))

(defn parse-typedef-property [raw]
  {:name (:name raw)
   :description (:description raw)
   :type (get-in raw [:type :names])})

(defn parse-typedef-properties [raw]
  (let [props (map parse-typedef-property (:properties raw))]
    {:properties props
     :has-properties (> (count props) 0)}))

(defn parse-typedef-base-types [raw]
  {:type (get-in raw [:type :names])})

(defn parse-typedef [raw top-level-callback sample-callback]
  (let [res 
        (merge (utils/parse-general-doclet raw sample-callback)
               (parse-typedef-base-types raw)
               (parse-typedef-properties raw)
               {:kind "typedef"})]
    (top-level-callback res)))
