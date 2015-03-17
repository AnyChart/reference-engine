(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [app.ajax :as ajax :refer [load-json]]
            [app.tree :refer [load-tree]]
            [app.search :refer [load-search-index]]
            [app.versions :refer [init-versions-events]]
            [app.resize :refer [init-resize]]
            [editors]
            [cljs.core.async :refer [chan put! take! timeout] :as async]
            [weasel.repl :as ws-repl]
            [clojure.walk :refer [prewalk]]
            [goog.net.XhrIo]
            [goog.style]
            [goog.json :as json]
            [goog.events]
            [goog.events.EventType]
            [goog.dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defn connect-repl []
  (ws-repl/connect "ws://localhost:9001"))

(defn- load-page [e]
  (if-not (is-open-in-new-tab e)
    (let [url (-> e .-target (.getAttribute "href"))]
      (if (app.ajax/is-current url)
        (.preventDefault e)
        (if (app.ajax/is-ajax url)
          (do
            (swap! search-state assoc :visible false)
            (.preventDefault e)
            (.pushState js/history nil nil url)
            (app.ajax/load-page url)))))))

(defn setup-node-link [component]
  (let [root (reagent/dom-node component)
        link (goog.dom/getElementByClass "node-link" root)]
    (goog.events.listen link
                        goog.events.EventType/CLICK
                        load-page)))

(defn remove-node-link [component]
  (let [root (reagent/dom-node component)
        link (goog.dom/getElementByClass "node-link" root)]
    (goog.events.removeAll link)))

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
  (init-versions-events)
  (init-resize)
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
  (load-search-index version))

;;(.reinit js/window)
