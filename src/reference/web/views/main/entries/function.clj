(ns reference.web.views.main.entries.function
  (:require [reference.web.views.common :as common]
            [clojure.string :as string]))


(defn function [data f]
  ;(println (:name f))
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
           [:a
            {:href          (str "#detailed-" (:name f) "-") ;; TODO index
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
                    [:th "Description"]
                    ]]
           [:tbody
            (for [p (:params f)]
              [:tr
               [:td (:name p)]
               [:td (string/join " | " (map #(common/link-or-text data %) (:types p)))]
               (when (:has-params-defaults f)
                 [:td [:pre.prettyprint (:default p)]])
               [:td (:description p)]])]
           ]])


       (when (:has-returns f)
         [:div.small-group
          [:p [:strong "Returns:"]]
          (for [r (:returns f)]
            (list
              (->> (:types r)
                   (map #(common/type-link data %))
                   (string/join " | "))
              " - "
              (:description r)))])
       (common/listing-and-samples data f)]]]]]

  )