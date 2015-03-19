(ns app.page
  (:require [goog.dom]
            [goog.style]
            [goog.events]
            [goog.events.EventType]
            [goog.dom.classes]))

(enable-console-print!)

(defn- nodelist-to-seq [nl]
  (if nl
    (let [result-seq (map #(aget nl %) (range (.-length nl)))]
      (doall result-seq))))

(defn- first-child [tag container]
  (aget (goog.dom.getElementsByTagNameAndClass tag nil container) 0))

(defn- tag-by-name-and-class [tag class el]
  (goog.dom.getElementsByTagNameAndClass tag class el))

(defn init-method [el]
  (let [tabs-header (aget (goog.dom.getElementsByTagNameAndClass "ul" "overrides" el) 0)
        tabs (goog.dom.getElementsByTagNameAndClass "li" nil tabs-header)
        tabs-content (aget (goog.dom.getElementsByTagNameAndClass "div" "overrides" el) 0)
        divs (goog.dom.getChildren tabs-content)]
    (goog.dom.classes/add (first-child "li" tabs-header) "active")
    (goog.dom.classes/add (first-child "div" tabs-content) "active")
    (doall (map (fn [li]
           (goog.events.listen
            (first-child "a" li)
            goog.events.EventType.CLICK
            (fn [e]
              (.preventDefault e)
              (let [index (.call Array.prototype.indexOf tabs li)]
                (goog.dom.classes/remove (aget
                                          (tag-by-name-and-class "li" "active" tabs-header)
                                          0) "active")
                (goog.dom.classes/remove (aget
                                          (tag-by-name-and-class "div" "active" tabs-content)
                                          0) "active")
                (goog.dom.classes/add (aget tabs index) "active")
                (goog.dom.classes/add (aget divs index) "active"))
              false)))
         (nodelist-to-seq tabs)))))

(defn- init-methods []
  (map init-method (nodelist-to-seq
                    (goog.dom.getElementsByTagNameAndClass "div" "method"))))

(defn init []
  (doall (init-methods)))
