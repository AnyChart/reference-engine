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

(defn parse-enum-field [raw links-prefix]
  {:name (:name raw)
   :description (utils/parse-description (:description raw) links-prefix)
   :has-description (not (clojure.string/blank? (:description raw)))
   :value (:defaultvalue raw)})

(defn do-parse-enum [raw links-prefix]
  (let [fields (map #(parse-enum-field % links-prefix) (:properties raw))]
    {:type (get-in raw [:type :names])
     :has-type (> (count (get-in raw [:type :names])) 0)
     :fields fields
     :has-fields (> (count fields) 0)
     :kind "enum"}))

(defn parse-enum [enum raw-data top-level-callback sample-callback links-prefix]
  (let [res (merge
             (if (and (is-link-to-another? enum)
                      (utils/inherit-doc? enum)
                      (enum-by-name (linked-enum-name enum)))
               (do-parse-enum (enum-by-name (linked-enum-name enum)) links-prefix)
               (do-parse-enum enum links-prefix))
             (utils/parse-general-doclet enum sample-callback links-prefix))]
    (top-level-callback res)))
