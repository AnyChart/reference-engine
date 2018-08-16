(ns reference.web.views.main.entries.constant
  (:require [reference.web.views.common :as common]))


(defn constant [data const]
  [:div.method-block.const-block
   [:div.panel-group
    [:div.panel.panel-default

     [:div.panel-heading
      [:h4.panel-title {:id (:name const)}
       [:a {:data-toggle "collapse"}
        (:name const)
        (when (:has-since const)
          [:span.pull-right (str "Since version " (:since const))])]]]

     [:div.panel-collapse.collapse.in
      [:div.panel-body
       [:div.small-group
        [:p (common/table-style (:description const))]

        (when (:has-detailed const)
          [:div.collapse-group
           [:a {:aria-expanded "true"
                :data-toggle   "collapse"}
            [:i.ac.ac-info]
            "Detailed description "]
           [:div.collapse-div.collapse.in {:aria-expanded "true"}
            [:div.collapse-content (:detailed const)]]])]

       (when (:type const)
         [:div.small-group
          [:p
           [:strong "Type: "]
           (common/link-or-text data (:type const))]])

       (common/listing-and-samples data const)

       ]]]]]

  )