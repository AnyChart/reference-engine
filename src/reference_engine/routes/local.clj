(ns reference-engine.routes.local
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [reference-engine.generator :as docs-gen]
            [clojure.java.io :refer [resource]]
            [clostache.parser :refer [render-resource]]))

(defn show-page [request]
  (if (docs-gen/is-updating)
    (str "please wait, rebuilding...")
    (let [path (get-in request [:params :page])
          namespaces (map :full-name (docs-gen/get-namespaces))
          info (docs-gen/get-local path)]
      (if info
        (render-resource "templates/app.mustache"
                         {:version "7"
                          :debug true
                          :main info
                          :kind {:namespace (= (:kind info) "namespace")
                                 :enum (= (:kind info) "enum")
                                 :class (= (:kind info) "class")}
                          :namespaces namespaces}
                         {:ns-part (slurp (resource "templates/ns.mustache"))
                          :fn-part (slurp (resource "templates/fn.mustache"))
                          :enum-part (slurp (resource "templates/enum.mustache"))
                          :class-part (slurp (resource "templates/class.mustache"))})
        (route/not-found "Source file not found")))))
 
(defroutes local-routes
  (GET "/:page" [] show-page))
