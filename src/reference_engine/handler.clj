(ns reference-engine.handler
  (:require [reference-engine.routes.index :refer [index-routes]]
            [reference-engine.routes.server :refer [server-routes setup-worker]]
            [reference-engine.routes.local :refer [local-routes]]
            [reference-engine.generator :as generator]
            [compojure.core :refer [routes]]
            [clojure.java.io :refer [file]]
            [clojure.java.shell :refer [sh]]
            [clojure.core.async :refer [go <!]]
            [org.httpkit.server :as server]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(def local-app (wrap-json-body
                (wrap-json-response
                 (routes local-routes index-routes))
                {:keywords? true}))

(def server-app (wrap-json-body
                 (wrap-json-response
                  (routes server-routes))
                 {:keywords? true}))

(defn start-server []
  (println "starting server http://localhost:9197/")
  (setup-worker)
  (server/run-server #'server-app {:port 9197}))

(defn init-for-repl []
  (let [path "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo"]
    (generator/init-local path)))

(defn -main [mode & args]
  (if (= mode "server")
    (start-server)))
