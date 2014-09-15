(ns reference-engine.handler
  (:require [reference-engine.routes.index :refer [index-routes]]
            [compojure.core :refer [routes]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]))

(def app (wrap-json-body
          (wrap-json-response
           (routes index-routes))
          {:keywords? true}))
