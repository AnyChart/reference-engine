(ns reference-engine.repl
  (:require [org.httpkit.server :as server]
            [reference-engine.handler :refer [app]]
            [clojure.repl :refer [doc]]))

(defonce server (atom nil))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start []
  (stop)
  (reset! server (server/run-server #'app {:port 9191})))

(defn restart []
  (stop)
  (start))
