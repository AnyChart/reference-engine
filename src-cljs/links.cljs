(ns links
  (:require [app.ajax]
            [reagent.core :as reagent]
            [goog.dom]
            [goog.events]
            [goog.events.EventType]))

(defn- is-open-in-new-tab [event]
  (or (.-ctrlKey event)
      (.-metaKey event)))

(defn- load-page [e]
  (.hide js/search)
  (if-not (is-open-in-new-tab e)
    (let [url (-> e .-target (.getAttribute "href"))]
      (if (app.ajax/is-current url)
        (.preventDefault e)
        (if (app.ajax/is-ajax url)
          (do
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
