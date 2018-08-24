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
     (common/styles-body commit)

     [:div#search-results-new {:style "display:none"}]

     [:div#ac-header
      (common/brand)
      [:div.pull-right.helpers
       [:select.selectpicker.versionselect {:data-width "130px"}
        (for [v versions]
          [:option {:value    v
                    :selected (when (= v version) "selected")}
           (str "Version " v)])]
       (common/support)]]

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
       [:div#tree-scr
        [:div#tree-menu
         [:ul.menu
          ;(tree-view data page)
          [:li.pull-down.group {:x-data-name "anychart"}
           [:a {:href "/anychart"} [:i.ac.ac-chevron-right] "anychart"]
           [:ul {:style "display:none"}]]
          ]]]]

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
      [:div#content-scr
       [:div#article-content
        [:div.content-container
         content]]
       (when footer
         [:div.content-footer [:hr] [:i.ac.ac-info] footer])]]
     ]))


(defn page [data]
  (hiccup-page/html5
    {:lang "en"}
    (common/head data)
    (body data)
    (resources/init-script data)
    (resources/google-tag-manager)))
