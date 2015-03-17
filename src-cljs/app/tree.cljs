(ns app.tree
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [chan put! take! timeout] :as async]
            [app.ajax :refer [load-json]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def expanded (reagent.core/atom {}))

(defn- get-title [node]
  (let [name (:name node)]
   (case (:kind node)
     "enum" (str "[" name "]")
     "typedef" (str "{" name "}")
     "function" (str name "()")
     "method" (str name "()")
     name)))

(defn- get-class [node]
  (:kind node))

(defn- is-group [node]
  (some #{(:kind node)} '("namespace" "class")))

(defn- is-expanded [node]
  (get @expanded (:full-name node)))

(defn- get-icon [node]
  (if (is-group node)
    [:i {:class (if (is-expanded node)
                  "icon-down-open"
                  "icon-right-open")}]))

(defn- get-link [node version]
  (str "/" version "/" (:full-name node)))

(defn- ignore-click? [event]
  (or (.-ctrlKey event)
      (.-metaKey event)))

(defn- node-click [event node version]
  (if (not (ignore-click? event))
    (do
      (.preventDefault event)
      (.stopPropagation event)
      (if (is-group node)
        (swap! expanded assoc (:full-name node) (not (is-expanded node))))
      false)))

(defn- tree-node [data version]
  [:li {:key (:full-name data) :class (get-class data) :href (get-link data version)}
   [:a {:on-click #(node-click % data version)} (get-icon data) (get-title data)]
   (if (and (is-group data) (is-expanded data))
     [:ul
      (map (fn [node] [tree-node node version]) (:children data))])])

(defn- tree-view [version tree]
  [:ul
   (map (fn [node] [tree-node node version]) tree)])

(defn- load-tree [version]
  (go
    (let [tree (<! (load-json version "tree.json"))]
      (reagent/render-component [tree-view version tree]
                                (.getElementById js/document "tree")))))
