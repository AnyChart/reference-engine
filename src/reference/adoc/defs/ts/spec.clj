(ns reference.adoc.defs.ts.spec
  (:require [clojure.spec.alpha :as s]))

(s/def :tree/namespaces (s/coll-of map?))
(s/def :tree/classes (s/coll-of map?))
(s/def :tree/typedefs (s/coll-of map?))
(s/def :tree/enums (s/coll-of map?))


(s/def ::tree (s/keys :req-un [:tree/classes
                               :tree/namespaces
                               :tree/typedefs
                               :tree/enums]))


;(defn t []
;  (let [data @reference.adoc.defs.typescript/top-level]
;    (s/valid? ::tree data)))