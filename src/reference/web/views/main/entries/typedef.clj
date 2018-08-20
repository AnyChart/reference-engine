(ns reference.web.views.main.entries.typedef
  (:require [reference.web.views.main.helpers :as helpers]))


(defn typedef [{:keys [main] :as data}]
  (list
    [:h1
     [:span.page-type "{typedef} "] (:full-name main)
     [:a#github-edit.btn.btn-default.btn-small.github-fork.pull-right {:href (helpers/edit-link)}
      [:span
       [:i.ac.ac-andrews-pitchfork]]
      " Improve this Doc"]]

    [:div.content-block
     [:div.small-group (:description main)]
     (helpers/listing-and-samples data main)]

    (when (:has-types main)
      [:div.content-block
       [:p "This type can contain one of the following types:"]
       [:ul.list.list-dotted
        (for [t (:type main)]
          [:li (helpers/type-link data t)])]])

    (when (:has-properties main)
      [:div.content-block
       [:table.table.table-condensed
        [:thead
         [:tr
          [:th "Name"]
          [:th "Type"]
          [:th "Description"]]]
        [:tbody
         (for [prop (:properties main)]
           [:tr
            [:td.name (:name prop)]
            [:td.code-style (helpers/compound-type data (:type prop))]
            [:td (:description prop)]])]]])))
