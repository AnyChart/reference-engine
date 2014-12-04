(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [weasel.repl :as ws-repl]))

(enable-console-print!)

(defn connect-repl []
  (ws-repl/connect "ws://localhost:9001"))

(defn- title [node]
  (let [name (:name node)]
   (case (:kind node)
     "enum" (str "[" name "]")
     "typedef" (str "{" name "}")
     "function" (str name "()")
     name)))

(defn- link [node version]
  (str "/" version "/"))

(defn- tree-node [version node]
  (println (:full-name node))
  [:li {:key (:full-name node)}
   [:a {:href (link node version)} (title node)]])

(defn- tree-view [version namespaces]
  [:ul (map #(tree-node version %) namespaces)])

(defn ^:export init [version tree]
  (reagent/render-component [tree-view version (js->clj tree :keywordize-keys true)]
                            (.getElementById js/document "tree"))
  (println "init!"))

(.reinit js/window)
