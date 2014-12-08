(ns app
  (:require [reagent.core :as reagent :refer [atom]]
            [weasel.repl :as ws-repl]))

(enable-console-print!)

(def opened-nodes (atom {}))

(defn connect-repl []
  (ws-repl/connect "ws://localhost:9001"))

(defn- title [node]
  (let [name (:name node)]
   (case (:kind node)
     "enum" (str "[" name "]")
     "typedef" (str "{" name "}")
     "function" (str name "()")
     name)))

(defn- link [node version]
  (str "/" version "/" (:full-name node)))

(defn- node-visible? [node]
  (let [key (:full-name node)]
    (and (contains? @opened-nodes key)
         (get @opened-nodes key))))

(defn- toggle-group [event node]
  (.preventDefault event)
  (let [key (:full-name node)
        visible (node-visible? node)]
    (swap! opened-nodes assoc key (not visible)))
  false)

(defn- toggle-link [node]
  [:a {:on-click #(toggle-group % node) :href "#"}
      [:i {:class (str "fa " (if (node-visible? node)
                               "fa-chevron-down"
                               "fa-chevron-right"))}]])

(defn- child-nodes [version node]
  (if (node-visible? node)
    [:ul
     (map #(tree-node version %) (:children node))]))

(defn- tree-node [version node]
  (if (or (= (:kind node) "namespace")
          (= (:kind node) "class"))
    [:li {:key (:full-name node)}
     [toggle-link node]
     [:a {:href (link node version)}
      (title node)]
     [child-nodes version node]]
    [:li {:key (:full-name node)}
     [:a {:href (link node version)} (title node)]]))

(defn- tree-view [version namespaces]
  [:ul (map #(tree-node version %) namespaces)])

(def search-state (atom {}))

(defn- search-results-row [version data]
  [:li {:key data}
   [:a {:href (str "/" version "/" data)} data]])

(defn- search-results-view [version]
  (if (:visible @search-state)
    (if (seq (:results @search-state))
      [:ul.search-results
       (map #(search-results-row version %) (:results @search-state))]
      [:ul.search-results
       [:li [:a "Nothing found"]]])))

(defn- search-view [version]
  [:div.search
   [:i.fa.fa-search]
   [:div
    [:input {:type "text"
             :placeholder "search for method in the tree"}]]
   [search-results-view version]])

(defn ^:export init [version tree]
  (reagent/render-component [tree-view version (js->clj tree :keywordize-keys true)]
                            (.getElementById js/document "tree"))
  (reagent/render-component [search-view version]
                            (.getElementById js/document "search")))

(.reinit js/window)
