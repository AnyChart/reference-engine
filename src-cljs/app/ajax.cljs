(ns app.ajax
  (:require [cljs.core.async :refer [chan put! take! timeout] :as async]
            [goog.net.XhrIo]
            [goog.net.EventType]
            [goog.events]
            [goog.events.EventType]
            [goog.dom]
            [goog.array]
            [clojure.string]
            [app.page])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def page-loader (atom nil))
(def current-page (atom nil))

(defn- cleanup-url [page]
  (clojure.string/replace page #"#.*" ""))

(defn is-current [link]
  (and (= (cleanup-url link) @current-page)
       (= (.indexOf link "#") -1)))

(defn is-ajax [link]
  (not= (cleanup-url link) @current-page))

(defn- load-page-data [page]
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

(defn- is-open-in-new-tab [event]
  (or (.-ctrlKey event)
      (.-metaKey event)))

(defn load-page [page]
  (go
    (let [data (<! (load-page-data (cleanup-url page)))]
      (reset! current-page (cleanup-url page))
      (show-loaded-page data)
      (update-links))))
  
(defn load-page-from-link [e url]
  (if-not (is-open-in-new-tab e)
    (if (is-current url)
      (.preventDefault e)
      (if (is-ajax url)
        (do
          (.preventDefault e)
          (.pushState js/history nil nil url)
          (load-page url))))))

(defn- navigation-callback [event]
  (let [page (.-pathname (.-location js/document))]
    (if-not (= (cleanup-url page) @current-page)
      (do
        (.preventDefault event)
        (load-page page)))))

(defn init-navigation []
  (reset! current-page (.-pathname js/location))
  (goog.events/listen js/window
                      goog.events.EventType/POPSTATE
                      navigation-callback))

;;(load-page "http://localhost:9197/develop/anychart.core.axes.Ticks")

(defn- link-click-handler [event link]
  (.stopPropagation event)
  (load-page-from-link event (.getAttribute link "href")))

(defn- update-links []
  (let [links (goog.dom/getElementsByClass "type-link"
                                               (goog.dom/getElement "content"))]
    (goog.array/map links
                    (fn [link]
                      (goog.events/listen link
                                          goog.events.EventType/CLICK
                                          (fn [e] (link-click-handler e link)))))))

(defn load-json [version data]
  (let [c (chan 1)]
    (goog.net.XhrIo/send
     (str "/" version "/data/" data)
     (fn [e]
       (if (.isSuccess (.-target e))
         (let [res (.getResponseJson (.-target e))]
           (put! c (js->clj res :keywordize-keys true))))))
    c))
