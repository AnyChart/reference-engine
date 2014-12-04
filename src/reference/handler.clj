(ns reference.handler
  (:require [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [org.httpkit.server :as server]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response redirect]]
            [clostache.parser :refer [render-resource]]
            [reference.data.versions :as v]
            [reference.data.pages :as p])
  (:gen-class))

(defn- show-default-version [request]
  (redirect (str "/" (v/default-version) "/anychart")))

(defn- show-default-ns [request]
  (let [version (get-in request [:params :version])]
    (redirect (str "/" version "/anychart"))))

(defn- get-page-json [request])

(defn- show-page [request]
  (println "show page called")
  (let [version (get-in request [:params :version])
        page (get-in request [:params :page])]
    (println "vp:" version page)
    (if (and (v/version-exists? version)
             (p/page-exists? version page))
      (render-resource "templates/app.mustache"
                       {:version version
                        :debug true
                        :versions (v/all-versions)
                        :static-version "7"
                        :content (p/get-page version page)
                        :tree (v/tree-json version)
                        :link #(str "/" version "/" %)})
      (route/not-found (str "Not found" version page)))))

(defn- update-all [request])

(defroutes app-routes
  (route/resources "/")
  (GET "/" [] show-default-version)
  (GET "/_plz_" [] update-all)
  (POST "/_plz_" [] update-all)
  (GET "/:version" [] show-default-ns)
  (GET "/:version/" [] show-default-ns)
  (GET "/:version/:page/json" [] get-page-json)
  (GET "/:version/:page" [] show-page)
  (route/not-found "not found"))

(def app (wrap-json-body
          (wrap-json-response (routes app-routes))
          {:keywords? true}))

(defn -main [& args])
