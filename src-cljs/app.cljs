(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [app.ajax :as ajax :refer [load-json]]
            [app.tree :refer [load-tree]]
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

(declare search-state)

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

(def search-state (atom {}))
(def search-index (atom []))

(defn- search-results-row-view [version data]
  [:li {:key data}
   [:a {:class "node-link" :href (str "/" version "/" data)} data]])

(def search-results-row
  (with-meta search-results-row-view
    {:component-did-mount
     (fn [this]
       (setup-node-link this))
     :component-will-unmount
     (fn [this]
       (remove-node-link this))}))

(defn- search-results-view [version]
  (if (:visible @search-state)
    (if (seq (:results @search-state))
      [:ul.search-results
       (map (fn [row]
              [search-results-row version row])
            (:results @search-state))]
      [:ul.search-results
       [:li [:a "Nothing found"]]])))

(defn- search-for [query]
  (take 50 (filter (fn [row]
                     (>= (.indexOf row query) 0))
                   @search-index)))

(defn- search-change [event]
  (let [query (-> event .-target .-value)
        results (search-for query)]
    (swap! search-state assoc
           :query query
           :visible (not (empty? query))
           :results results)))

(defn- search-view [version tree]
  [:div.search
   [:i.fa.fa-search]
   [:div
    [:input {:type "text"
             :placeholder "search for method in the tree"
             :on-change search-change
             :value (:query @search-state)}]]
   [search-results-view version]])

(defn- load-search-index [version]
  (go
    (let [index-data (<! (load-json version "search.json"))]
      (reset! search-index index-data)
      (reagent/render-component [search-view version]
                                (.getElementById js/document "search")))))

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
  (load-search-index version))

;;(.reinit js/window)
