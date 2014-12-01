(ns reference.generator.core
  (:require [reference.generator.parser :as parser]
            [reference.generator.jsdoc :as jsdoc]
            [reference.generator.git :as git]
            [reference.generator.struct :as struct]
            [reference.generator.exports :as exports]
            [reference.generator.generator :as html-gen]
            [reference.generator.tree :as tree-gen]))

(defn- get-namespaces [exports-data & paths]
  (exports/remove-not-exported
   (-> (apply concat (map jsdoc/get-jsdoc paths))
       parser/parse
       struct/build)
   exports-data))

(defn- get-top-level [namespaces]
  (reduce (fn [res namespace]
            (concat res
                    [namespace]
                    (:classes namespace)
                    (:enums namespace)
                    (:typedefs namespace)))
          [] namespaces))

(println "======")
(let [src-path "/Users/alex/Work/anychart/reference-engine/data/repo/src/data"
      exports-data (exports/add-export-from-folder src-path)
      nses (get-namespaces exports-data src-path)]
  (html-gen/pre-render-top-level "develop" (get-top-level nses))
  (println (tree-gen/generate-tree nses)))
