(ns reference.web.views.page404.page404
  (:require [reference.web.views.resources :as resources]
            [reference.web.views.common :as common]
            [hiccup.page :as hiccup-page]))


(defn body [{:keys [title url description commit page-name] :as data}]
  [:body
   "<!--[if !IE]> -->"
   [:link {:rel "stylesheet" :type "text/css" :href "https://cdn.anychart.com/fonts/2.0.0/anychart.css",}]
   [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap/css/bootstrap.min.css"}]
   [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap-select.min.css"}]
   [:link {:rel "stylesheet" :type "text/css" :href "/lib/jquery-custom-content-scroller/jquery.mCustomScrollbar.min.css"}]
   [:link {:rel "stylesheet" :type "text/css" :href "/lib/prettiffy/prettify-tomorrow.css"}]
   [:link {:rel "stylesheet" :type "text/css" :href (str "/css/style.css?v=" commit)}]
   "<!-- <![endif]-->"

   [:div#ac-header
    [:a.navbar-brand {:href  "//anychart.com/"
                      :title "AnyChart Home"}
     [:img {:alt    "AnyChart"
            :height "72"
            :width  "300"
            :src    "/i/AnyChart-light-empty.png"}]
     [:div.chart-row
      [:span.chart-col.green]
      [:span.chart-col.orange]
      [:span.chart-col.red]]]
    [:a.brand.hidden-small-645 {:href  "/"
                                :title "AnyChart API Reference"}
     "API Reference"]

    [:div.pull-right.helpers

     [:div.text-muted.questions.hidden-small-410
      [:a.text-support {:href "//support.anychart.com/"}
       [:i.ac.ac-support]]
      [:span.hidden-super-small "Still have questions?"
       [:br]
       [:a {:href "http://anychart.com/support/"}
        " Contact support"]]]]]

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
         [:span.ac.ac-search.form-control-feedback]
         ]]]]]]
   ]
  )


(defn page [data]
  (hiccup-page/html5
    {:lang "en"}
    (common/head data)
    (body data)
    ;(resources/init-script-fast data)
    (resources/google-tag-manager)))
