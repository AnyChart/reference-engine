(ns reference.generator.core
  (:require [reference.generator.parser :as parser]
            [reference.generator.jsdoc :as jsdoc]
            [reference.generator.git :as git]
            [reference.generator.struct :as struct]
            [reference.generator.exports :as exports]))

(defn build [exports-data & paths]
  (exports/remove-not-exported
   (-> (apply concat (map jsdoc/get-jsdoc paths))
      parser/parse
      struct/build)
   exports-data))

(println "======")
(let [src-path "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src/data"
      exports-data (exports/add-export-from-folder src-path)]
  (def res (build exports-data src-path)))

(println "count:" (count res))
(println res)
