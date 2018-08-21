(ns reference.web.views.page404.page404
  (:require [reference.web.views.resources :as resources]
            [reference.web.views.common :as common]
            [hiccup.page :as hiccup-page]))


(defn body [{:keys [title url description commit page-name] :as data}]
  [:body
   (common/styles commit)

   [:div#ac-header
    (common/brand)
    [:div.pull-right.helpers
     (common/support)]]

   [:div.container-fluid
    [:div.row
     [:div.col-md-12
      [:div.content404
       [:h1 "Error 404"]
       [:p "This page you were trying to reach at this address doesn't seem to exist.
This is usually the result of a bad or outdated link. We apologize for any inconvenience."]
       [:p "You can try:"
        [:ul
         [:li "Start with  "
          [:a {:href "/"} "API Main Page"]]
         [:li "Search "
          [:a {:href "/"} "api.anychart.com"] ":"]]
        [:div.form-group.has-feedback
         [:input.form-control.input-sm {:placeholder "What are you looking for?" :type "text"}]
         [:span.ac.ac-search.form-control-feedback]]]]]]]])


(defn page [data]
  (hiccup-page/html5
    {:lang "en"}
    (common/head data)
    (body data)
    ;(resources/init-script-fast data)
    (resources/google-tag-manager)))
