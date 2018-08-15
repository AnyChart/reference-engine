(ns reference.web.views.main.entries.method
  (:require [reference.web.views.common :as common]
            [clojure.string :as string]))


(defn method [data f]
  ;(println "method: " (:name f))
  [:div.method-block
   [:h3 {:id (:name f)} (:name f)]

   [:div.panel-group {:id (str "accordion-" (:name f))}

    (for [[index o] (map-indexed vector (:overrides f))
          :let [is-last (= (inc index) (count (:overrides f)))]]

      [:div.panel.panel-default

       [:div.panel-heading
        [:h4.panel-title
         [:a {:href        (str "#accordion-" (:name f) "-" index)
              :data-parent (str "#accordion-" (:name f))
              :data-toggle "collapse"}
          (:signature o)
          (when (:has-since o)
            [:span.pull-right (str "Since version " (:since o))])]]]

       [:div.pannel-collapse.collapse {:id    (str "accordion-" (:name f) "-" index)
                                       :class (common/is-override-expand f is-last index)}
        [:div.panel-body

         [:div.small-group
          (:description o)
          (when (:has-detailed o)
            [:div.collapse-group
             [:a.collapsed {:href          (str "#detailed-" (:name f) "-" index)
                            :aria-expanded "true"
                            :data-toggle   "collapse"}
              [:i.ac.ac-info]
              " Detailed description"]

             [:div.collapse-div.collapse {:id            (str "detailed-" (:name f) "-" index)
                                          :aria-expanded "false"}
              [:div.collapse-content
               (:detailed o)]]])]

         (when (:has-params o)
           [:div.small-group
            [:p [:strong "Params:"]]
            [:table.table.table-bordered
             [:thead
              [:tr
               [:th "Name"]
               [:th "Type"]
               (when (:has-params-defaults o) [:th "Default"])
               [:th "Description"]]]
             [:tbody
              (for [p (:params o)]
                [:tr
                 [:td (:name p)]
                 [:td (string/join " | " (map #(common/link-or-text data %) (:types p)))]
                 (when (:has-params-defaults o)
                   [:td [:pre.prettyprint (:default p)]])
                 [:td (:description p)]])]]])

         (when (:has-returns o)
           [:div.small-group
            [:p [:strong "Returns:"]]
            (for [r (:returns o)]
              (list
                (->> (:types r)
                     (map #(common/type-link data %))
                     (string/join " | "))
                " - "
                (:description r)))])

         (common/listing-and-samples data o)

         ]]])
    ]])