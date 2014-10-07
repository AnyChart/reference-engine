(ns reference-engine.handler
  (:require [reference-engine.routes.index :refer [index-routes]]
            [reference-engine.routes.local :refer [local-routes]]
            [reference-engine.generator :as generator]
            [compojure.core :refer [routes]]
            [async-watch.core :refer [changes-in cancel-changes]]
            [clojure.java.io :refer [file]]
            [clojure.java.shell :refer [sh]]
            [clojure.core.async :refer [go <!]]
            [org.httpkit.server :as server]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]])
  (:gen-class))

(def app (wrap-json-body
          (wrap-json-response
           (routes local-routes index-routes))
          {:keywords? true}))

(defn start-server [])

(defn start-local [path]
  (println "generating local documentation:" path)
  (generator/init-local path)
  (sh "open" (str "http://localhost:9191/" (generator/get-first-namespace)))
  (println "starting server http://localhost:9191/")
  (println "############################")
  (println "Press r<RET> to rebuild docs")
  (go (while true
        (let [c (read-line)]
          (if (= (clojure.string/lower-case c) "r")
            (generator/init-local path)
            (println "Unknown command: " c)))))
  (server/run-server #'app {:port 9191}))

(defn init-for-repl []
  (let [path "/Users/alex/Work/anychart/reference-engine/data/acdvf/repo/src"]
    (generator/init-local path)))

(defn -main [mode & args]
  (if (= mode "server")
    (start-server)
    (start-local mode)))
