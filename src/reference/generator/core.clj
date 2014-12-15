(ns reference.generator.core
  (:require [reference.generator.parser :as parser]
            [reference.generator.jsdoc :as jsdoc]
            [reference.generator.git :as git]
            [reference.generator.struct :as struct]
            [reference.generator.exports :as exports]
            [reference.generator.generator :as html-gen]
            [reference.generator.tree :as tree-gen]
            [taoensso.timbre :as timbre :refer [info]]))

(defn get-namespaces [version exports-data & paths]
  (info "get-namespaces" version)
  (let [res (exports/remove-not-exported
             (-> (apply concat (map jsdoc/get-jsdoc paths))
                 (parser/parse version)
                 struct/build)
             exports-data)]
    (info "generated namespaces count:" (count res))
    res))

(defn get-top-level [namespaces]
  (info "get-top-level" (count namespaces))
  (reduce (fn [res namespace]
            (concat res
                    [namespace]
                    (:classes namespace)
                    (:enums namespace)
                    (:typedefs namespace)))
          [] namespaces))
