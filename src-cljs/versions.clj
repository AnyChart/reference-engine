(ns app.versions
  (:require [goog.dom]
            [goog.style]))

(defn- toggle-versions [event]
  (let [ul (goog.dom/getElement "version-toggle")
        visible (goog.style/isElementShown ul)]
    (goog.style/setElementShown ul (not visible))))

(defn init-versions-events []
  (goog.events/listen (goog.dom/getElement "version-toggler")
                      goog.events.EventType/CLICK
                      toggle-versions))
