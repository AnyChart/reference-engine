(ns reference.generator.core
  (:require [reference.generator.parser :as parser]
            [reference.generator.jsdoc :as jsdoc]
            [reference.generator.git :as git]
            [reference.generator.struct :as struct]))

(defn build [& paths]
  (-> (apply concat (map jsdoc/get-jsdoc paths))
      parser/parse
      struct/build))

(println "======")
(def res
  (time
   (build "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/data")))

(println "count:" (count res))
(println "classes:" (count (:classes res)))
