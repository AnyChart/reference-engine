(ns reference-engine.parser.typedefs
  (:require [reference-engine.parser.utils :as utils]))

(defn typedef? [raw]
  (= (:kind raw) "typedef"))

(defn parse-typedef-property [raw links-prefix]
  {:name (:name raw)
   :description (utils/parse-description (:description raw) links-prefix)
   :type (get-in raw [:type :names])})

(defn parse-typedef-properties [raw links-prefix]
  (let [props (map #(parse-typedef-property % links-prefix) (:properties raw))]
    {:properties props
     :has-properties (> (count props) 0)}))

(defn parse-typedef-base-types [raw]
  {:type (get-in raw [:type :names])})

(defn parse-typedef [raw top-level-callback sample-callback links-prefix]
  (let [res 
        (merge (utils/parse-general-doclet raw sample-callback links-prefix)
               (parse-typedef-base-types raw)
               (parse-typedef-properties raw links-prefix)
               {:kind "typedef"})]
    (top-level-callback res)))
