(ns search
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [chan put! take! timeout] :as async]
            [links]
            [data])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def search-state (atom {}))

(defn ^:export hide []
  (swap! search-state assoc :visible false))

(def search-index (atom []))

(defn- search-results-row-view [version data]
  [:li {:key (+ data (.random js/Math))}
   [:a {:class "node-link" :href (str "/" version "/" data)} data]])

(def search-results-row
  (with-meta search-results-row-view
    {:component-did-mount
     (fn [this]
       (links/setup-node-link this))
     :component-will-unmount
     (fn [this]
       (links/remove-node-link this))}))

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
  (sort (take 50 (filter (fn [row]
                           (and row
                                (>= (.indexOf (.toLowerCase row) (.toLowerCase query)) 0)))
                         @search-index))))

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
             :value (:query @search-state)}]]])

(defn load-search-index [version]
  (go
    (let [index-data (<! (data/load-json version "search.json"))]
      (reset! search-index (set index-data))
      (reagent/render-component [search-view version]
                                (.getElementById js/document "search"))
      (reagent/render-component [search-results-view version]
                                (.getElementById js/document "search-results")))))
