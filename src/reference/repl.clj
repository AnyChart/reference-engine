(ns reference.repl
  (:require [org.httpkit.server :as server]
            [reference.handler :refer [app]]))

(defonce server (atom nil))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start []
  (stop)
  (reset! server (server/run-server #'app {:port 9197})))

(defn restart []
  (stop)
  (start))