(ns reference-engine.handler
  (:require [reference-engine.routes.index :refer [index-routes]]
            [reference-engine.routes.local :refer [local-routes]]
            [reference-engine.generator :reref [generate-local]]
            [compojure.core :refer [routes]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(def app (wrap-json-body
          (wrap-json-response
           (routes local-routes index-routes))
          {:keywords? true}))

(defn -main [& args]
 )
