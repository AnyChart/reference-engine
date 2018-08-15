(ns reference.web.views.main.entries.namespace-entry
  (:require [reference.web.views.common :as common]
            [reference.web.views.main.entries.constant :as constant-view]
            [reference.web.views.main.entries.method :as method-view]
            [reference.web.views.main.entries.function :as function-view]

            ))


(defn namespace' [{:keys [main] :as data}]
  (println (:name main))
  (list
    [:h1
     [:span.page-type "namespace "]
     (:full-name main)
     [:a#github-edit.btn.btn-default.btn-small.github-fork.pull-right
      {:href (common/edit-link)}
      [:span [:i.ac.ac-andrews-pitchfork]]
      " Improve this Doc"]]

    [:div.content-block
     [:p (:description main)]]

    (when (:has-constants main)
      [:div.content-block.methods
       [:h2 "Constants Overview"]
       [:table.table.table-condensed
        [:tbody
         [:tr
          [:td.th.empty {:colspan "2"}]]
         (for [c (:constants main)]
           [:tr
            [:td.name
             [:a.type-link {:href (str "#" (:name c))} (:name c)]]
            [:td (:short-description c)]])]]])


    (when (:has-functions main)
      [:div.content-block.methods
       [:h2 "Functions Overview"]
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
                  [:td (:short-description f)]]))))
         (when-not (:has-categories main)
           (list
             [:tr [:td.th.empty {:colspan "2"}]]
             (for [f (:functions main)]
               [:tr
                [:td.name [:a.type-link {:href (str "#" (:name f))} (str (:name f) "()")]]
                [:td (:short-description f)]])))
         ]]
       ])

    (when (:has-enums main)
      [:div.content-block.methods
       [:h2 "Enums Overview"]
       [:table.table.table-condensed
        [:tbody
         [:tr [:td.th.empty {:colspan "2"}]]
         (for [enum (:enums main)]
           [:tr
            [:td.name
             [:a.type-link {:href (common/link data (:name enum))} (:name enum)]]
            [:td (:short-description enum)]])]]])

    (when (:has-typedefs main)
      [:div.content-block.methods
       [:h2 "Typedefs Overview"]
       [:table.table.table-condensed
        [:tbody
         [:tr [:td.th.empty {:colspan "2"}]]
         (for [typedef (:typedefs main)]
           [:tr
            [:td.name
             [:a.type-link {:href (common/link data (:name typedef))} (:name typedef)]]
            [:td (:short-description typedef)]])]]])

    (when (:has-classes main)
      [:div.content-block.methods
       [:h2 "Classes Overview"]
       [:table.table.table-condensed
        [:tbody
         [:tr [:td.th.empty {:colspan "2"}]]
         (for [c (:classes main)]
           [:tr
            [:td.name
             [:a.type-link {:href (common/link data (:name c))} (:name c)]]
            [:td (:short-description c)]])]]])

    (when (:has-constants main)
      [:div.content-block.methods
       [:h2 "Constants Description"]
       (for [c (:constants main)]
         (constant-view/constant data c))])

    (when (:has-functions main)
      [:div.content-block.methods
       [:h2 "Functions Description"]
       (for [f (:functions main)]
         (if (:overrides f)
           (method-view/method data f)
           (function-view/function data f)))]

      )

    )
  )