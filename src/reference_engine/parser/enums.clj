(ns reference-engine.parser.enums
  (:require [reference-engine.parser.utils :as utils]))

(defn enum? [raw]
  (and (:isEnum raw)
       (= (:kind raw) "member")
       (utils/static? raw)))

(defn is-link-to-another? [raw]
  (= (get-in raw [:meta :code :type]) "MemberExpression"))

(defn linked-enum-name [raw]
  (get-in raw [:meta :code :value]))

(defn enum-by-name [name raw-data]
  (filter #(and (enum? %)
                (= (:longname %) name))))

(defn parse-enum-field [raw]
  {:name (:name raw)
   :description (:description raw)
   :has-description (not (clojure.string/blank? (:description raw)))
   :value (:defaultvalue raw)})

(defn do-parse-enum [raw]
  (let [fields (map parse-enum-field (:properties raw))]
    {:type (get-in raw [:type :names])
     :has-type (> (count (get-in raw [:type :names])) 0)
     :fields fields
     :has-fields (> (count fields) 0)
     :kind "enum"}))

(defn parse-enum [enum raw-data]
  (let [res (merge
             (if (and (is-link-to-another? enum)
                      (utils/inherit-doc? enum)
                      (enum-by-name (linked-enum-name enum)))
               (do-parse-enum (enum-by-name (linked-enum-name enum)))
               (do-parse-enum enum))
             (utils/parse-general-doclet enum))]
    (utils/cache-entry res)))
