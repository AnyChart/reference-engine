(ns reference-engine.routes.server
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]
            [clojure.java.io :refer [resource]]
            [clostache.parser :refer [render-resource]]
            [org.httpkit.client :as http]
            [cheshire.core :refer [generate-string]]
            [reference-engine.projects :as prj]))

(defn generate-type-link [project version]
  (fn [text]
    (fn [render-fn]
      (let [type (render-fn text)]
        (render-fn text)))))
        ;;(if (prj/has-entry project version type)
        ;;  (str "<a href='/" project "/" version "/" type "'>" type "</a>")
        ;;  type)))))

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
                          :versions (sort (prj/versions project))
                          :project project
                          :default-ns (prj/namespace-default project version)
                          :kind {:namespace (= (:kind info) "namespace")
                                 :enum (= (:kind info) "enum")
                                 :class (= (:kind info) "class")
                                 :typedef (= (:kind info) "typedef")}
                          :namespaces (prj/namespaces project version)
                          :link #(str "/" project "/" version "/" %)
                          :type-link (fn [text]
                                       (fn [render-fn]
                                         (let [type (render-fn text)]
                                           (if (prj/has-entry project version type)
                                              (str "<a href='/" project "/" version "/" type "'>" type "</a>")
                                             type))))}
                         {:ns-part (slurp (resource "templates/ns.mustache"))
                          :fn-part (slurp (resource "templates/fn.mustache"))
                          :enum-part (slurp (resource "templates/enum.mustache"))
                          :class-part (slurp (resource "templates/class.mustache"))
                          :typedef-part (slurp (resource "templates/typedef.mustache"))
                          :examples (slurp (resource "templates/example.mustache"))}))
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

(defn notify-slack [project versions]
  (if (not (empty? versions))
    (http/post "https://anychart-team.slack.com/services/hooks/incoming-webhook?token=P8Z59E0kpaOqTcOxner4P5jb"
               {:form-params {:payload (generate-string {:text (str "<http://api.anychart.dev> API reference updated for " project " updated branches: " (clojure.string/join ", " versions))
                                                         :channel "#notifications"
                                                         :username "api-reference"})}})))

(defn update-project [request]
  (let [project (get-in request [:params :project])]
    (if (prj/exists? project)
      (let [versions (filter #(not (= % nil))
                             (prj/update-project project))]
        (notify-slack project versions)
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
