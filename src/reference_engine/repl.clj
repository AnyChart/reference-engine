(ns reference-engine.repl
  (:require [org.httpkit.server :as server]
            [reference-engine.handler :as handler]
            [clojure.repl :refer [doc]]))

(defonce server (atom nil))

(defn stop []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn start []
  (stop)
  (handler/init-for-repl)
  (reset! server (server/run-server #'handler/local-app {:port 9191})))

(defn restart []
  (stop)
  (start))
