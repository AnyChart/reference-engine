(ns reference-engine.parser.functions
  (:require [reference-engine.parser.utils :as utils]))

(defn function? [raw]
  (= (:kind raw) "function"))

(defn parse-return [raw-return]
  {:description (:description raw-return)
   :optional (:optional raw-return)
   :default (:defaultvalue raw-return)
   :type (get-in raw-return [:type :names])})

(defn parse-param [raw-param]
  (assoc (parse-return raw-param)
    :name (:name raw-param)))

(defn parse-params [raw]
  (let [res (map parse-param (:params raw))]
    {:params res
     :has-params (> (count res) 0)}))

(defn parse-returns [raw]
  (let [res (map parse-return (:returns raw))]
    {:returns res
     :has-returns (> (count res) 0)}))

(defn parse-params-signature [raw]
  {:params-signature
   (str "(" (clojure.string/join ", " (get-in raw [:meta :code :paramnames])) ")")})

(defn parse-meta [raw]
  (if (utils/inherit-doc? raw)
    {:inherit-doc true}))

(defn parse-function [raw sample-callback]
  (merge (utils/parse-general-doclet raw sample-callback)
         (parse-params-signature raw)
         (parse-params raw)
         (parse-returns raw)
         (parse-meta raw)))
