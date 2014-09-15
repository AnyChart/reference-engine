(ns reference-engine.routes.index
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [clostache.parser :refer [render-resource]]))

(defn show-landing [request]
  (render-resource "templates/app.mustache" {:version "7"
                                             :debug true}))
 
(defroutes index-routes
  (GET "/" [] show-landing)
  (route/resources "/")
  (route/not-found "Page not found"))
