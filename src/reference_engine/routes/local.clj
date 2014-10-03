(ns reference-engine.routes.local
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [reference-engine.generator :as docs-gen]
            [clojure.java.io :refer [resource]]
            [clostache.parser :refer [render-resource]]))

(defn show-page [request]
  (let [path (get-in request [:params :page])
        namespaces (map :full-name (docs-gen/get-namespaces))
        info (docs-gen/get-local path)]
    (if info
      (render-resource "templates/app.mustache"
                       {:version "7"
                        :debug true
                        :main info
                        :namespaces namespaces}
                       {:ns-part (slurp (resource "templates/ns.mustache"))
                        :fn-part (slurp (resource "templates/fn.mustache"))
                        :enum-part (slurp (resource "templates/enum.mustache"))})
      (route/not-found "Source file not found"))))
 
(defroutes local-routes
  (GET "/d/:page" [] show-page))
