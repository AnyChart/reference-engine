(ns app.ajax
  (:require [cljs.core.async :refer [chan put! take! timeout] :as async]
            [app.editors]
            [goog.net.XhrIo]
            [goog.net.EventType]
            [goog.events]
            [goog.events.EventType]
            [goog.dom]
            [goog.array]
            [clojure.string])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def page-loader (atom nil))
(def current-page (atom nil))

(defn- cleanup-url [page]
  (clojure.string/replace page #"#.*" ""))

(defn- load-page-data [page]
  (println "load page")
  (if @page-loader
    (.abort @page-loader))
  
  (let [c (chan 1)]
    (aset (goog.dom/getElement "content") "innerHTML" "Loading...")
    (reset! page-loader (goog.net.XhrIo.))
    (.listen @page-loader
             goog.net.EventType/COMPLETE
             (fn [e]
               (if (.isSuccess (.-target e))
                 (let [res (js->clj (.getResponseJson (.-target e)) :keywordize-keys true)]
                   (reset! page-loader nil)
                   (put! c res)))))
    (.send @page-loader (str page "/data"))
    c))

(defn- show-loaded-page [data]
  (aset (goog.dom/getElement "content") "innerHTML" (:content data))
  (aset (goog.dom/getElement "current-path") "innerHTML" (:page data))
  (app.editors/init-editors))

(declare update-links)

(defn load-page [page]
  (go
    (let [data (<! (load-page-data (cleanup-url page)))]
      (reset! current-page (cleanup-url page))
      (show-loaded-page data)
      (update-links))))
  
(defn load-page-from-link [event page]
  (if (or (.-ctrlKey event)
          (.-metaKey event)
          (= (cleanup-url page) @current-page))
    true
    (do (.preventDefault event)
        (.stopPropagation event)
        (load-page page)
        (.pushState js/history nil nil page)
        false)))

(defn- navigation-callback []
  (if-not (= (cleanup-url page) @current-page)
    (load-page (.-pathname (.-location js/document)))))

(defn init-navigation []
  (reset! current-page (.-pathname js/location))
  (goog.events/listen js/window
                      goog.events.EventType/POPSTATE
                      navigation-callback))

;;(load-page "http://localhost:9197/develop/anychart.core.axes.Ticks")


(defn- link-click-handler [event link]
  (load-page-from-link event (.getAttribute link "href")))

(defn- update-links []
  (let [links (goog.dom/getElementsByClass "type-link"
                                               (goog.dom/getElement "content"))]
    (goog.array/map links
                    (fn [link]
                      (goog.events/listen link
                                          goog.events.EventType/CLICK
                                          (fn [e] (link-click-handler e link)))))))
