(ns reference-engine.parser.constants
  (:require [reference-engine.parser.utils :refer [contains-tag? get-tag parse-general-doclet]]
            [clojure.string :refer [trim]]))

(defn parse-constant-tag [tag]
  (if-let [matches (re-matches #"\{(.*)\}(.*)" tag)]
    {:type (trim (nth matches 1))
     :short-description (trim (nth matches 2))}))

(defn constant? [raw]
  (and (= (:kind raw) "member")
       (contains-tag? raw "define")
       (= (:scope raw) "static")))

(defn get-constant-value [raw]
  (get-in raw [:meta :code :value]))

(defn parse-constant [raw sample-callback]
  (merge (parse-general-doclet raw sample-callback)
         (parse-constant-tag (first (get-tag raw "define")))
         {:value (get-constant-value raw)
          :kind "constant"}))
