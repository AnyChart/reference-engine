(ns reference.web.views.main.entries.class-entry
  (:require [reference.web.views.common :as common]
            [reference.web.views.main.entries.method :as method-view]))


(defn class' [{:keys [main] :as data}]
  ;(println data)
  (list
    [:h1
     [:span.page-type "class "]
     (:full-name main)
     [:a#github-edit.btn.btn-default.btn-small.github-fork.pull-right {:href (common/edit-link)}
      [:span [:i.ac.ac-andrews-pitchfork]]
      " Improve this Doc"]]

    [:div.content-block
     [:p.bottom-space
      (when (:has-extends main)
        (list
          "Extends: "
          (for [extend (:extends main)]
            [:a.type-link {:href (common/link data extend)} extend])))]
     [:p (:description main)]
     (common/listing-and-samples data main)]

    (when (:has-methods main)
      [:div.content-block.methods
       [:h2 "Methods Overview"]
       [:table.table.table-condensed
        [:tbody
         (when (:has-categories main)
           (for [category (:categories main)]
             (list
               [:tr
                [:td.th.empty {:colspan "2"}
                 [:a.category.type-link {:id   (str "category-" (:id category))
                                         :href (str "#category-" (:id category))}
                  (:name category)]]]
               (for [member (:members category)]
                 [:tr
                  [:td.name
                   [:a.type-link {:href (str "#" (:name member))}
                    (str (:name member) "()")]]
                  [:td (:short-description member)]]))))

         (when-not (:has-categories main)
           [:tr
            [:td.th {:colspan "2"}]]
           (for [method (:methods main)]
             [:tr
              [:td.name
               [:a.type-link {:href (str "#" (:name method))}
                (str (:name method) "()")]]
              [:td (:short-description method)]]))
         ]]])

    (when (:has-methods main)
      [:div.content-block.methods
       [:h2 "Methods Description"]
       (for [method (:methods main)]
         (method-view/method data method))])
    )
  )