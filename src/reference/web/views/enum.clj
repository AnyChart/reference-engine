(ns reference.web.views.enum
  (:require [reference.web.views.common :as common]
            [reference.util.utils :as utils]))


(defn get-categories [enum-fields]
  (let [groups (group-by :category enum-fields)
        ; names (filter some? (sort (keys groups)))
        misc-group (get groups nil)
        groups (dissoc groups nil)]
    [groups misc-group]))


(defn enum-field-sample [{:keys [main version is-url-version show-samples playground] :as data} entry]
  (when (and show-samples (:has-playground-samples entry))
    [:div.small-group
     (for [s (:playground-samples entry)]
       [:a.code-style {:target "_blank"
                       :href   (common/playground-link data (:file s))}
        (:title s)])]))


(defn enum [{:keys [main version is-url-version show-samples playground] :as data}]
  [:div
   [:h1
    [:span.page-type "[enum] "]
    (:full-name main)
    [:a#github-edit.btn.btn-default.btn-small.github-fork.pull-right {:href "https://github.com/AnyChart/api.anychart.com"}
     [:span [:i.ac.ac-andrews-pitchfork]]
     "Improve this Doc"]]

   [:div.content-block
    [:div.small-group (:description main)]
    (common/samples data main)]

   (when (:has-fields main)
     [:div.content-block
      [:table.table.table-condensed
       [:thead
        [:tr
         [:th "Value"]
         [:th "Description"]
         [:th {:width "20%"} "Example"]]]
       (let [[categories misc-group] (get-categories (:fields main))]
         [:tbody
          ;(categories (:fields main))
          ;(for [f (:fields main)]
          ;  [:tr
          ;   [:td.code-style (:value f)]
          ;   [:td (:description f)]
          ;   [:td (common/samples data f)]])

          (for [[category-name fields] categories]
            (cons
              [:tr
               [:td.th.empty {:colspan 3}
                [:a.category.type-link {:id   (str "category-" (utils/name->url category-name))
                                        :href (str "#category-" (utils/name->url category-name))}
                 category-name]]]
              (for [f fields]
                [:tr
                 [:td.code-style (:value f)]
                 [:td (:description f)]
                 [:td (enum-field-sample data f)]])))
          (when (and (seq categories) (seq misc-group))
            [:tr
             [:td.th.empty {:colspan 3}
              [:a.category.type-link {:id   "category-misc"
                                      :href "#category-misc"}
               "Miscellaneous"]]])
          (when (seq misc-group)
            (for [f misc-group]
              [:tr
               [:td.code-style (:value f)]
               [:td (:description f)]
               [:td (enum-field-sample data f)]]))])]])])