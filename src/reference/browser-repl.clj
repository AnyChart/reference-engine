(ns reference.browser-repl
  (:require [weasel.repl.websocket]))

(defn start []
  (cemerick.piggieback/cljs-repl
   :repl-env (weasel.repl.websocket/repl-env
              :ip "0.0.0.0" :port 9001)))
