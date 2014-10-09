(ns reference-engine.routes.index
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [clostache.parser :refer [render-resource]]))
 
(defroutes index-routes
  (route/resources "/")
  (route/not-found "Page not found"))
