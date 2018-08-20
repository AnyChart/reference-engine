(ns reference.web.views.main.entries.namespace-entry
  (:require [reference.web.views.main.helpers :as helpers]
            [reference.web.views.main.entries.constant :as constant-view]
            [reference.web.views.main.entries.method :as method-view]
            [reference.web.views.main.entries.function :as function-view]))


(defn namespace' [{:keys [main] :as data}]
  (list
    [:h1
     [:span.page-type "namespace "] (:full-name main)
     [:a#github-edit.btn.btn-default.btn-small.github-fork.pull-right {:href (helpers/edit-link)}
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
         (for [const (:constants main)]
           [:tr
            [:td.name
             [:a.type-link {:href (str "#" (:name const))} (:name const)]]
            [:td (:short-description const)]])]]])


    (when (:has-functions main)
      [:div.content-block.methods
       [:h2 "Functions Overview"]
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
           (list
             [:tr
              [:td.th.empty {:colspan "2"}]]
             (for [func (:functions main)]
               [:tr
                [:td.name [:a.type-link {:href (str "#" (:name func))} (str (:name func) "()")]]
                [:td (:short-description func)]])))
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
             [:a.type-link {:href (helpers/link data (:name enum))} (:name enum)]]
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
             [:a.type-link {:href (helpers/link data (:name typedef))} (:name typedef)]]
            [:td (:short-description typedef)]])]]])

    (when (:has-classes main)
      [:div.content-block.methods
       [:h2 "Classes Overview"]
       [:table.table.table-condensed
        [:tbody
         [:tr [:td.th.empty {:colspan "2"}]]
         (for [cls (:classes main)]
           [:tr
            [:td.name
             [:a.type-link {:href (helpers/link data (:name cls))} (:name cls)]]
            [:td (:short-description cls)]])]]])

    (when (:has-constants main)
      [:div.content-block.methods
       [:h2 "Constants Description"]
       (for [const (:constants main)]
         (constant-view/constant data const))])

    (when (:has-functions main)
      [:div.content-block.methods
       [:h2 "Functions Description"]
       (for [func (:functions main)]
         (if (:overrides func)
           (method-view/method data func)
           (function-view/function data func)))]

      )
    )
  )