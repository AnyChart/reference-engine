(ns reference.web.views.main.main-page
  (:require [hiccup.page :as hiccup-page]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [reference.web.tree :as tree]
            [reference.web.views.resources :as resources]
            [reference.web.views.common :as common]
            [clojure.string :as string]
            [hiccup.core :as hiccup]))


(defn tree-view [{:keys [version is-url-version tree]} url]
  (let [entries (json/parse-string tree true)]
    (string/join (map #(tree/tree-view-partial %
                                               version
                                               is-url-version
                                               url) entries))))


(defn body [{:keys [commit
                    versions
                    version
                    tree
                    page
                    is-last
                    last-version
                    content
                    footer] :as data}]
  (hiccup/html
    [:body
     "<!--[if !IE]> -->"
     [:link {:rel "stylesheet" :type "text/css" :href "https://cdn.anychart.com/fonts/2.0.0/anychart.css",}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap/css/bootstrap.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap-select.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/jquery-custom-content-scroller/jquery.mCustomScrollbar.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/prettiffy/prettify-tomorrow.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href (str "/css/style.css?v=" commit)}]
     "<!-- <![endif]-->"

     [:div#search-results-new {:style "display:none"}]

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
       [:select.selectpicker.versionselect {:data-width "130px"}
        (for [v versions]
          [:option {:value    v
                    :selected (when (= v version) "selected")}
           (str "Version " v)])]
       [:div.text-muted.questions.hidden-small-410
        [:a.text-support {:href "//support.anychart.com/"}
         [:i.ac.ac-support]]
        [:span.hidden-super-small "Still have questions?"
         [:br]
         [:a {:href "http://anychart.com/support/"}
          " Contact support"]]]]]

     [:div#menu-bar
      [:a.switcher " "
       [:i.ac.ac-chevron-left-thick]]
      [:div#search-form
       [:form.form-horizontal {:role "form"}
        [:div.form-group.has-feedback
         [:div.col-sm-12
          [:input#search-input.form-control {:autocomplete "off"
                                             :placeholder  "search"
                                             :type         "text"}]
          [:div#search-results {:style "background-color: white;"}]
          [:span.ac.ac-search.form-control-feedback]]]]]

      [:div#tree-wrapper
       [:div#tree-menu
        [:ul.menu
         ;(tree-view data page)
         [:li.pull-down.group {:x-data-name "anychart"}
          [:a {:href "/anychart"} [:i.ac.ac-chevron-right] "anychart"]
          [:ul {:style "display:none"}]]
         ]]]

      [:div#footer
       [:div#footer-inner
        [:a.soc-network {:target "_blank"
                         :href   "https://www.facebook.com/AnyCharts"}
         [:span.soc-network-icon.fb [:i.sn-mini-icon.ac.ac-facebook]]]
        [:a.soc-network {:target "_blank"
                         :href   "https://twitter.com/AnyChart"}
         [:span.soc-network-icon.tw [:i.sn-mini-icon.ac.ac-twitter]]]
        [:a.soc-network {:target "_blank"
                         :href   "https://www.linkedin.com/company/386660"}
         [:span.soc-network-icon.in [:i.sn-mini-icon.ac.ac-linkedin]]]

        [:p (str " © " (t/year (t/now)) " AnyChart.Com All rights reserved.")]]]

      [:div#size-controller]]

     [:ol.breadcrumb]
     [:a#top-page-content "Top "
      [:i.ac.ac-arrow-up]]

     (when-not is-last
       [:div#warning.warning-version.alert.alert-default.fade.in
        [:button.close {:aria-hidden  "true"
                        :data-dismiss "alert"
                        :type         "button"}
         "×"]
        [:i.ac.ac-exclamation] (str " You are looking at an outdated " version " version of this document. Switch to the ")
        [:a {:href              (str "/try/" page)
             :data-last-version last-version}
         last-version]
        " version to see the up to date information."])

     [:div#content-wrapper
      [:div#article-content
       [:div.content-container
        content]]

      (when footer
        [:div.content-footer [:hr] [:i.ac.ac-info] footer])]
     ]))


(defn page [data]
  (hiccup-page/html5
    {:lang "en"}
    (common/head data)
    (body data)
    ;(resources/init-script data)
    (resources/init-script-fast data)
    (resources/google-tag-manager)))
