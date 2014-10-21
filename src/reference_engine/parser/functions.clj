(ns reference-engine.parser.functions
  (:require [reference-engine.parser.utils :as utils]))

(defn function? [raw]
  (= (:kind raw) "function"))

(defn parse-return [raw-return links-prefix]
  {:description (utils/parse-description (:description raw-return) links-prefix)
   :optional (:optional raw-return)
   :default (:defaultvalue raw-return)
   :type (get-in raw-return [:type :names])})

(defn parse-param [raw-param links-prefix]
  (assoc (parse-return raw-param links-prefix)
    :name (:name raw-param)))

(defn parse-params [raw links-prefix]
  (let [res (map #(parse-param % links-prefix) (:params raw))]
    {:params res
     :has-params (> (count res) 0)}))

(defn parse-returns [raw links-prefix]
  (let [res (map #(parse-return % links-prefix) (:returns raw))]
    {:returns res
     :has-returns (> (count res) 0)}))

(defn parse-params-signature [raw]
  {:params-signature
   (str "(" (clojure.string/join ", " (get-in raw [:meta :code :paramnames])) ")")})

(defn parse-meta [raw]
  (if (utils/inherit-doc? raw)
    {:inherit-doc true}))

(defn parse-function [raw sample-callback links-prefix]
  (assoc (merge (utils/parse-general-doclet raw sample-callback links-prefix)
                (parse-params-signature raw)
                (parse-params raw links-prefix)
                (parse-returns raw links-prefix)
                (parse-meta raw))
    :kind "function"))
