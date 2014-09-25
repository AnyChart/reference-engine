(ns reference-engine.parser.fields
  (:require [reference-engine.parser.utils :as utils]))

(defn field? [raw]
  (and (= (:kind raw) "member")
       (> (count (get-in raw [:type :names])) 0)
       (not (:isEnum raw))))

(defn parse-field [raw]
  (assoc (utils/parse-general-doclet raw)
    :type (get-in raw [:type :names])
    :static (utils/static? raw)))
