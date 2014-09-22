(ns reference-engine.routes.local
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [reference-engine.generator :as docs-gen]
            [clostache.parser :refer [render-resource]]))

(defn show-page [request]
  (let [path (get-in request [:params :page])]
    (if (docs-gen/doc-exists? path)
      (docs-gen/generate-dev-version path)
      (route/not-found "Source file not found"))))
 
(defroutes local-routes
  (GET "/d/:page" [] show-page))
