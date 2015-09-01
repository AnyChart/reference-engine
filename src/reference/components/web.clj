(ns reference.components.web
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :as server]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [taoensso.timbre :as timbre :refer [info]]
            [reference.web.routes :refer [app]]))

(defn- component-middleware [component app]
  (fn [request]
    (app (assoc request :component component))))

(defn- create-web-app [component]
  (wrap-json-response
   (wrap-json-body
    (component-middleware component #'app)
    {:keywords? true})))

(defrecord Web [config web-server jdbc redis]
  component/Lifecycle

  (start [this]
    (assoc this :web-server (server/run-server (create-web-app this) config)))

  (stop [this]
    (if web-server
      (web-server :timeout 100))
    (assoc this :web-server nil)))

(defn new-web [config]
  (map->Web {:config config}))
