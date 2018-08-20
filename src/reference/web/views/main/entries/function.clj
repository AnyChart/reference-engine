(ns reference.web.views.main.entries.function
  (:require [reference.web.views.main.helpers :as helpers]
            [clojure.string :as string]))


(defn function [data f]
  [:div.method-block
   [:h3 {:id (:name f)} (:name f)]
   [:div.panel-group
    [:div.panel.panel-default
     [:div.panel-heading
      [:h4.panel-title
       [:a {:data-toggle "collapse"}
        (:signature f)
        (when (:has-since f)
          [:span.pull-right (str "Since version " (:since f))])]]]

     [:div.panel-collapse.collapse.in
      [:div.panel-body
       [:div.small-group
        [:p (:description f)]

        (when (:has-detailed f)
          [:div.collapse-group
           [:a {:href          (str "#detailed-" (:name f) "-") ;; TODO index
                :aria-expanded "true"
                :data-toggle   "collapse"}
            [:i.ac.ac-info]
            " Detailed description"]
           [:div.collapse-div.collapse {:id            (str "detailed-" (:name f) "-") ;; TODO index
                                        :aria-expanded "false"}
            [:div.collapse-content (:detailed f)]]])]

       (when (:has-params f)
         [:div.small-group
          [:p [:strong "Params:"]]
          [:table.table.table-bordered
           [:thead [:tr
                    [:th "Name"]
                    [:th "Type"]
                    (when (:has-params-defaults f)
                      [:th "Default"])
                    [:th "Description"]]]
           [:tbody
            (for [param (:params f)]
              [:tr
               [:td (:name param)]
               [:td (helpers/compound-type data (:types param))]
               (when (:has-params-defaults f)
                 [:td [:pre.prettyprint (:default param)]])
               [:td (:description param)]])]
           ]])

       (when (:has-returns f)
         [:div.small-group
          [:p [:strong "Returns:"]]
          (for [return (:returns f)]
            (list
              (helpers/compound-type data (:types return))
              " - "
              (:description return)))])

       (helpers/listing-and-samples data f)]]]]])