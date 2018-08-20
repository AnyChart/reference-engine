(ns reference.web.views.main.entries.constant
  (:require [reference.web.views.main.helpers :as helpers]))


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
        [:p (helpers/table-style (:description const))]

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
           (helpers/link-or-text data (:type const))]])

       (helpers/listing-and-samples data const)

       ]]]]]

  )