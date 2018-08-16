(ns reference.web.views.main.entries.method
  (:require [reference.web.views.common :as common]
            [clojure.string :as string]
            [hiccup.core :as hiccup]))


(defn method [data method]
  ;(println "method: " (:name f))
  [:div.method-block
   [:h3 {:id (:name method)} (:name method)]

   [:div.panel-group {:id (str "accordion-" (:name method))}

    (for [[index override] (map-indexed vector (:overrides method))
          :let [is-last (= (inc index) (count (:overrides method)))]]

      [:div.panel.panel-default

       [:div.panel-heading
        [:h4.panel-title
         [:a {:href        (str "#accordion-" (:name method) "-" index)
              :data-parent (str "#accordion-" (:name method))
              :data-toggle "collapse"}
          (:signature override)
          (when (:has-since override)
            [:span.pull-right (str "Since version " (:since override))])]]]

       [:div.pannel-collapse.collapse {:id    (str "accordion-" (:name method) "-" index)
                                       :class (common/is-override-expand method is-last index)}
        [:div.panel-body
         [:div.small-group
          (:description override)
          (when (:has-detailed override)
            [:div.collapse-group
             [:a.collapsed {:href          (str "#detailed-" (:name method) "-" index)
                            :aria-expanded "true"
                            :data-toggle   "collapse"}
              [:i.ac.ac-info]
              " Detailed description"]

             [:div.collapse-div.collapse {:id            (str "detailed-" (:name method) "-" index)
                                          :aria-expanded "false"}
              [:div.collapse-content
               (:detailed override)]]])]

         (when (:has-params override)
           [:div.small-group
            [:p [:strong "Params:"]]
            [:table.table.table-bordered
             [:thead
              [:tr
               [:th "Name"]
               [:th "Type"]
               (when (:has-params-defaults override) [:th "Default"])
               [:th "Description"]]]
             [:tbody
              (for [param (:params override)]
                [:tr
                 [:td (:name param)]
                 [:td (common/compound-type data (:types param))]
                 (when (:has-params-defaults override)
                   [:td [:pre.prettyprint (:default param)]])
                 [:td (:description param)]])]]])

         (when (:has-returns override)
           [:div.small-group
            [:p [:strong "Returns:"]]
            (for [return (:returns override)]
              (list
                (common/compound-type data (:types return))
                " - "
                (:description return)))])

         (common/listing-and-samples data override)

         ]]])
    ]])