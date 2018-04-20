(ns reference.web.views.common
  (:require [hiccup.core :as hiccup]
            [clojure.string :as string]))


(defn playground-link [{:keys [version is-url-version playground]} val]
  (str "//" playground "/api"
       (when is-url-version (str "/" version))
       (string/trim val) "-plain"))


(defn samples [{:keys [main version is-url-version show-samples playground] :as data} entry]
  (when (and show-samples (:has-playground-samples entry))
    [:div.small-group
     [:p
      [:strong "Try it:"]]
     [:ul.list.list-dotted
      (for [s (:playground-samples entry)]
        [:li
         [:a.code-style {:target "_blank"
                         :href   (playground-link data (:file s))}
          (:title s)]])]]))


(defn listing-and-samples [data f])