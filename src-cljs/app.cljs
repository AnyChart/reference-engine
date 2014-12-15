(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [app.ajax :as ajax]
            [editors]
            [search]
            [links]
            [data]
            [cljs.core.async :refer [chan put! take! timeout] :as async]
            [weasel.repl :as ws-repl]
            [clojure.walk :refer [prewalk]]
            [goog.style]
            [goog.json :as json]
            [goog.events]
            [goog.events.EventType]
            [goog.dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def opened-nodes (atom {}))

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
  (str "/" version "/" (:full-name node)))

(defn- node-visible? [node]
  (let [key (:full-name node)]
    (and (contains? @opened-nodes key)
         (get @opened-nodes key))))

(defn- toggle-group [event node]
  (.preventDefault event)
  (.stopPropagation event)
  (let [key (:full-name node)
        visible (node-visible? node)]
    (swap! opened-nodes assoc key (not visible)))
  false)

(defn- toggle-link [node]
  [:a {:on-click #(toggle-group % node)}
      [:i {:class (str "fa " (if (node-visible? node)
                               "fa-chevron-down"
                               "fa-chevron-right"))}]])

(declare tree-node)

(defn- child-nodes [version node]
  (if (node-visible? node)
    [:ul
     (map (fn [el]
            [tree-node {:version version :node el}])
          (:children node))]))


(defn tree-node-view [{:keys [version node]}]
  (if (or (= (:kind node) "namespace")
          (= (:kind node) "class"))
    [:li {:key (:full-name node)}
     [toggle-link node]
     [:a {:class "node-link" :href (link node version)}
      (title node)]
     [child-nodes version node]]
    [:li {:key (:full-name node)}
     [:a {:class "node-link" :href (link node version)} (title node)]]))

(def tree-node
  (with-meta tree-node-view
    {:component-did-mount
     (fn [this]
       (links/setup-node-link this))
     :component-will-unmount
     (fn [this]
       (links/remove-node-link this))}))

(defn- tree-view [version namespaces]
  [:ul (map (fn [ns]
              [tree-node {:version version :node ns}])
            namespaces)])

(defn- load-tree [version]
  (go
    (let [tree (<! (data/load-json version "tree.json"))]
      (reagent/render-component [tree-view version tree]
                                (.getElementById js/document "tree")))))

(defn- toggle-versions [e]
  (let [ul (goog.dom/getElement "version-toggle")
        visible (goog.style/isElementShown ul)]
    (goog.style/setElementShown ul (not visible))))

(defn- resize-tree [e]
  (let [screen-x (.-screenX e)
        max-x (- (.-width (goog.dom/getViewportSize)) 500)
        x (if (< screen-x 229)
            229
            (if (> screen-x max-x)
              max-x
              screen-x))
        pix (str x "px")]
    (goog.style/setStyle (goog.dom/getElement "sidebar") "width" pix)
    (goog.style/setStyle (goog.dom/getElement "main") "left" pix)))

(defn- start-tree-resize [e]
  (goog.style/showElement (goog.dom/getElement "locker")
                          true)
  (goog.events/listen (goog.dom/getElement "locker")
                      goog.events.EventType/MOUSEMOVE
                      resize-tree))

(defn- stop-tree-resize [e]
  (goog.style/showElement (goog.dom/getElement "locker")
                          false)
  (goog.events/unlisten (goog.dom/getElement "locker")
                        goog.events.EventType/MOUSEMOVE
                        resize-tree))

(defn- init-events []
  (goog.events/listen (goog.dom/getElement "version-toggler")
                      goog.events.EventType/CLICK
                      toggle-versions)
  (goog.events/listen (goog.dom/getElement "resizer")
                      goog.events.EventType/MOUSEDOWN
                      start-tree-resize)
  (goog.events/listen (goog.dom/getElement "locker")
                      goog.events.EventType/MOUSEUP
                      stop-tree-resize))

(defn ^:export init [version]
  (init-events)
  (editors/init-editors)
  (ajax/init-navigation)
  (load-tree version)
  (search/load-search-index version))

;;(.reinit js/window)
