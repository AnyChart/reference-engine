(ns reference-engine.parser.fields
  (:require [reference-engine.parser.utils :as utils]))

(defn field? [raw]
  (and (= (:kind raw) "member")
       (> (count (get-in raw [:type :names])) 0)
       (not (:isEnum raw))))

(defn parse-field [raw sample-callback links-prefix]
  (assoc (utils/parse-general-doclet raw sample-callback links-prefix)
    :type (get-in raw [:type :names])
    :static (utils/static? raw)
    :kind "field"))
