(ns reference-engine.routes.local
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [reference-engine.generator :as docs-gen]
            [clojure.java.io :refer [resource]]
            [cheshire.core :refer [generate-string]]
            [ring.util.response :refer [response]]
            [clostache.parser :refer [render-resource]]))

(defn show-page-json [request]
  (if (docs-gen/is-updating)
    (str "please wait, rebuilding...")
    (let [path (get-in request [:params :page])
          namespaces (map :full-name (docs-gen/get-namespaces))
          info (docs-gen/get-local path)]
      (if info
        (let [content (render-resource
                      "templates/app-json.mustache"
                      {:link #(str %)
                       :type-link #(str %)
                       :main info
                       :kind {:namespace (= (:kind info) "namespace")
                              :enum (= (:kind info) "enum")
                              :class (= (:kind info) "class")
                              :typedef (= (:kind info) "typedef")}}
                       {:ns-part (slurp (resource "templates/ns.mustache"))
                        :fn-part (slurp (resource "templates/fn.mustache"))
                        :enum-part (slurp (resource "templates/enum.mustache"))
                        :class-part (slurp (resource "templates/class.mustache"))
                        :typedef-part (slurp (resource "templates/typedef.mustache"))
                        :examples (slurp (resource "templates/example.mustache"))})]
          (response {:content content
                     :page path}))
          (route/not-found "not found o!o")))))

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
                          :versions []
                          :project ""
                          :v ""
                          :link #(str %)
                          :type-link #(str %)
                          :kind {:namespace (= (:kind info) "namespace")
                                 :enum (= (:kind info) "enum")
                                 :class (= (:kind info) "class")
                                 :typedef (= (:kind info) "typedef")}
                          :tree @docs-gen/local-tree}
                         {:ns-part (slurp (resource "templates/ns.mustache"))
                          :fn-part (slurp (resource "templates/fn.mustache"))
                          :enum-part (slurp (resource "templates/enum.mustache"))
                          :class-part (slurp (resource "templates/class.mustache"))
                          :typedef-part (slurp (resource "templates/typedef.mustache"))
                          :examples (slurp (resource "templates/example.mustache"))})
        (route/not-found "not found o!o")))))
 
(defroutes local-routes
  (GET "/:page/json" [] show-page-json)
  (GET "/:page" [] show-page))
