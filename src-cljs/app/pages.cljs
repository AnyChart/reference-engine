(ns app.pages
  (:require [app.ajax :as ajax]
            [reagent.core :as reagent]
            [goog.net.XhrIo]
            [goog.events]
            [goog.events.EventType]
            [goog.dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn ignore-click? [event]
  (or (.-ctrlKey event)
      (.-metaKey event)))

(defn load-page [e]
  (if-not (ignore-click? e)
    (let [url (-> e .-target (.getAttribute "href"))]
      (if (app.ajax/is-current url)
        (.preventDefault e)
        (if (app.ajax/is-ajax url)
          (do
            ;;(swap! search-state assoc :visible false)
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
