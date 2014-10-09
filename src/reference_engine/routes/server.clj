(ns reference-engine.routes.server
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]
            [clojure.java.io :refer [resource]]
            [clostache.parser :refer [render-resource]]
            [reference-engine.projects :as prj]))

(defn show-page [request]
  (let [project (get-in request [:params :project])
        version (get-in request [:params :version])
        page (get-in request [:params :page])]
    (if (and (prj/exists? project)
             (prj/version-exists? project version)
             (prj/has-entry project version page))
      (let [info (prj/get-entry project version page)]
        (render-resource "templates/app.mustache"
                         {:version "7"
                          :v version
                          :debug true
                          :main info
                          :kind {:namespace (= (:kind info) "namespace")
                                 :enum (= (:kind info) "enum")
                                 :class (= (:kind info) "class")}
                          :namespaces (prj/namespaces project version)
                          :link #(str "/" project "/" version "/" %)}
                         {:ns-part (slurp (resource "templates/ns.mustache"))
                          :fn-part (slurp (resource "templates/fn.mustache"))
                          :enum-part (slurp (resource "templates/enum.mustache"))
                          :class-part (slurp (resource "templates/class.mustache"))}))
      (route/not-found "Not found :("))))

(defn show-default-ns [request]
  (let [project (get-in request [:params :project])
        version (get-in request [:params :version])]
    (if (prj/exists? project)
      (if (prj/version-exists? project version)
        (let [page (prj/namespace-default project version)]
          (if page
            (redirect (str "/" project "/" version "/" page))
            (route/not-found "Not found :(")))
        (route/not-found "Unknown version"))
      (route/not-found "Project not found"))))

(defn update-project [request]
  (let [project (get-in request [:params :project])]
    (if (prj/exists? project)
      (do
        (prj/update-project project)
        "Updated!")
      (route/not-found "not found"))))

(defn update-all [request]
  (prj/update)
  "Updated!")

(defroutes server-routes
  (route/resources "/")
  (GET "/_plz_" [] update-all)
  (GET "/:project/_plz_" [] update-project)
  (POST "/:project/_plzz_" [] update-project)
  (GET "/:project/:version" [] show-default-ns)
  (GET "/:project/:version/" [] show-default-ns)
  (GET "/:project/:version/:page" [] show-page))
