(ns reference.web.views.main.entries.constant
  (:require [reference.web.views.common :as common]))


(defn constant [data c]
  [:div.method-block.const-block
   [:div.panel-group
    [:div.panel.panel-default

     [:div.panel-heading
      [:h4.panel-title {:id (:name c)}
       [:a {:data-toggle "collapse"}
        (:name c)
        (when (:has-since c)
          [:span.pull-right (str "Since version " (:since c))])]]]

     [:div.panel-collapse.collapse.in
      [:div.panel-body
       [:div.small-group
        [:p (common/table-style (:description c))]

        (when (:has-detailed c)
          [:div.collapse-group
           [:a {:aria-expanded "true"
                :data-toggle   "collapse"}
            [:i.ac.ac-info]
            "Detailed description "]
           [:div.collapse-div.collapse.in
            {:aria-expanded "true"}
            [:div.collapse-content (:detailed c)]]])]

       (when (:type c)
         [:div.small-group
          [:p
           [:strong "Type: "]
           (common/link-or-text data (:type c))]])

       (common/listing-and-samples data c)

       ]]]]]

  )