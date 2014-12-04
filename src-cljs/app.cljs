(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [weasel.repl :as ws-repl]))

(enable-console-print!)

(defn connect-repl []
  (ws-repl/connect "ws://localhost:9001"))

(defn- tree-node [node])

(defn- tree-view [root])

(defn ^:export init [version tree]
  (println tree)
  (println "init!"))
