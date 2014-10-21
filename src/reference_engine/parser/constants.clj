(ns reference-engine.parser.constants
  (:require [reference-engine.parser.utils :refer [contains-tag? get-tag parse-general-doclet parse-description]]
            [clojure.string :refer [trim]]))

(defn parse-constant-tag [tag links-prefix]
  (if-let [matches (re-matches #"\{(.*)\}(.*)" tag)]
    {:type (trim (nth matches 1))
     :short-description (parse-description (trim (nth matches 2)) links-prefix)}))

(defn constant? [raw]
  (and (= (:kind raw) "member")
       (contains-tag? raw "define")
       (= (:scope raw) "static")))

(defn get-constant-value [raw]
  (get-in raw [:meta :code :value]))

(defn parse-constant [raw sample-callback links-prefix]
  (merge (parse-general-doclet raw sample-callback links-prefix)
         (parse-constant-tag (first (get-tag raw "define")) links-prefix)
         {:value (get-constant-value raw)
          :kind "constant"}))
