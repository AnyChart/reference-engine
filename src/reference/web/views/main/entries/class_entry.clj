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
          (for [e (:extends main)]
            [:a.type-link {:href (common/link data e)} e])))]
     [:p (:description main)]
     (common/listing-and-samples data main)]

    (when (:has-methods main)
      [:div.content-block.methods
       [:h2 "Methods Overview"]
       [:table.table.table-condensed
        [:tbody
         (when (:has-categories main)
           (for [c (:categories main)]
             (list
               [:tr
                [:td.th.empty {:colspan "2"}
                 [:a.category.type-link {:id   (str "category-" (:id c))
                                         :href (str "#category-" (:id c))}

                  (:name c)]]]
               (for [f (:members c)]
                 [:tr
                  [:td.name [:a.type-link {:href (str "#" (:name f))}
                             (str (:name f) "()")]]
                  [:td (:short-description f)]])))
           )
         (when-not (:has-categories main)
           [:tr [:td.th {:colspan "2"}]]
           (for [f (:methods main)]
             [:tr
              [:td.name [:a.type-link {:href (str "#" (:name f))}
                         (str (:name f) "()")]]
              [:td (:short-description f)]]))
         ]
        ]
       ])

    (when (:has-methods main)
      [:div.content-block.methods
       [:h2 "Methods Description"]
       (for [f (:methods main)]
         (method-view/method data f)
         )])
    )

  )