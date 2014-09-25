(ns reference-engine.parser.interfaces
  (:require [reference-engine.parser.utils :as utils]))

(defn interface? [raw]
  (and (= (:kind raw) "function")
       (utils/static? raw)
       (utils/contains-tag? raw "interface")))

(defn parse-interface [raw raw-data])
