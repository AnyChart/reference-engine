(ns app.search
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [chan put! take! timeout] :as async]
            [goog.events]
            [goog.events.EventType]
            [goog.dom]
            [app.ajax :refer [load-json]]
            [app.pages :refer [setup-node-link remove-node-link]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def state (atom {}))
(def index (atom []))

(defn- result-row [version name]
  [:li {:key name}
   [:a {:class "node-link" :href (str "/" version "/" name)} name]])

(def wrapped-result-row
  (with-meta result-row
    {:component-did-mount
     (fn [this]
       (setup-node-link this))
     :component-will-unmount
     (fn [this]
       (remove-node-link this))}))

(defn- search-results [version]
  (if (:visible @state)
    (if (seq (:results @state))
      [:ul
       (map (fn [row] [result-row version row])
            (:results @state))]
      [:ul
       [:li [:a "Nothing found"]]])))

(defn- search-for [query]
  (take 50 (filter #(>= (.indexOf % query) 0) @index)))

(defn- search-change [event]
  (let [query (-> event .-target .-value)
        results (search-for query)]
    (swap! state assoc
           :query query
           :visible (not (empty? query))
           :results results)))

(defn- search-view [version]
  [:div.search
   [:i.icon-search]
   [:div.input-container
    [:input#search {:type "text"
                    :placeholder "search for method"
                    :on-change search-change
                    :value (:query @state)}]]])

(defn- load-search-index [version]
  (go
    (let [index-data (<! (load-json version "search.json"))]
      (reset! index index-data)
      (reagent/render-component [search-view version]
                                (.getElementById js/document "search-bar"))
      (reagent/render-component [search-results version]
                                (.getElementById js/document "search-results")))))
