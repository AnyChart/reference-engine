(ns reference.web.views.main.main-page
  (:require [hiccup.page :as hiccup-page]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [reference.web.tree :as tree]
            [reference.web.views.resources :as resources]
            [clojure.string :as string]
            [hiccup.core :as hiccup]))


(defn tree-view [{:keys [version is-url-version tree]} url]
  (let [entries (json/parse-string tree true)]
    (string/join (map #(tree/tree-view-partial %
                                               version
                                               is-url-version
                                               url) entries))))


(defn head [{:keys [title url description commit page-name] :as data}]
  (hiccup/html
    [:head
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]

     [:link {:rel "icon" :type "image/x-icon" :href "/i/anychart.ico"}]
     [:link {:rel "canonical" :href url}]

     [:meta {:property "og:title" :content title}]
     [:meta {:property "og:type" :content "website"}]
     [:meta {:property "og:url" :content url}]
     [:meta {:property "og:image" :content "https://cdn.anychart.com/images/features-laptop.png"}]
     [:meta {:property "og:site_name" :content "AnyChart"}]
     [:meta {:property "og:locale" :content "en_US"}]
     [:meta {:property "og:description" :content description}]

     [:meta {:name "keywords" :content (str (when page-name (str page-name ","))
                                            "anychart api reference, js charts, javascript charts, html5 charts, ajax charts, plots, line charts, bar charts, pie charts, js maps, javascript gantt charts, js dashboard")}]
     [:meta {:name "description" :content description}]

     [:title title]

     "<!--[if IE]>"
     [:link {:rel "stylesheet" :type "text/css" :href "https://cdn.anychart.com/fonts/2.0.0/anychart.css",}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap/css/bootstrap.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/bootstrap-select.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/jquery-custom-content-scroller/jquery.mCustomScrollbar.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "/lib/prettiffy/prettify-tomorrow.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href (str "/css/style.css?v=" commit)}]
     "<![endif]-->"

     [:script {:src "/lib/jquery-2.1.3.min.js"}]
     [:script {:src "/lib/jquery-custom-content-scroller/jquery.mousewheel-3.0.6.min.js"}]
     [:script "/*! head.load - v1.0.3 */
             (function(n,t){\"use strict\";function w(){}function u(n,t){if(n){typeof n==\"object\"&&(n=[].slice.call(n));for(var i=0,r=n.length;i<r;i++)t.call(n,n[i],i)}}function it(n,i){var r=Object.prototype.toString.call(i).slice(8,-1);return i!==t&&i!==null&&r===n}function s(n){return it(\"Function\",n)}function a(n){return it(\"Array\",n)}function et(n){var i=n.split(\"/\"),t=i[i.length-1],r=t.indexOf(\"?\");return r!==-1?t.substring(0,r):t}function f(n){(n=n||w,n._done)||(n(),n._done=1)}function ot(n,t,r,u){var f=typeof n==\"object\"?n:{test:n,success:!t?!1:a(t)?t:[t],failure:!r?!1:a(r)?r:[r],callback:u||w},e=!!f.test;return e&&!!f.success?(f.success.push(f.callback),i.load.apply(null,f.success)):e||!f.failure?u():(f.failure.push(f.callback),i.load.apply(null,f.failure)),i}function v(n){var t={},i,r;if(typeof n==\"object\")for(i in n)!n[i]||(t={name:i,url:n[i]});else t={name:et(n),url:n};return(r=c[t.name],r&&r.url===t.url)?r:(c[t.name]=t,t)}function y(n){n=n||c;for(var t in n)if(n.hasOwnProperty(t)&&n[t].state!==l)return!1;return!0}function st(n){n.state=ft;u(n.onpreload,function(n){n.call()})}function ht(n){n.state===t&&(n.state=nt,n.onpreload=[],rt({url:n.url,type:\"cache\"},function(){st(n)}))}function ct(){var n=arguments,t=n[n.length-1],r=[].slice.call(n,1),f=r[0];return(s(t)||(t=null),a(n[0]))?(n[0].push(t),i.load.apply(null,n[0]),i):(f?(u(r,function(n){s(n)||!n||ht(v(n))}),b(v(n[0]),s(f)?f:function(){i.load.apply(null,r)})):b(v(n[0])),i)}function lt(){var n=arguments,t=n[n.length-1],r={};return(s(t)||(t=null),a(n[0]))?(n[0].push(t),i.load.apply(null,n[0]),i):(u(n,function(n){n!==t&&(n=v(n),r[n.name]=n)}),u(n,function(n){n!==t&&(n=v(n),b(n,function(){y(r)&&f(t)}))}),i)}function b(n,t){if(t=t||w,n.state===l){t();return}if(n.state===tt){i.ready(n.name,t);return}if(n.state===nt){n.onpreload.push(function(){b(n,t)});return}n.state=tt;rt(n,function(){n.state=l;t();u(h[n.name],function(n){f(n)});o&&y()&&u(h.ALL,function(n){f(n)})})}function at(n){n=n||\"\";var t=n.split(\"?\")[0].split(\".\");return t[t.length-1].toLowerCase()}function rt(t,i){function e(t){t=t||n.event;u.onload=u.onreadystatechange=u.onerror=null;i()}function o(f){f=f||n.event;(f.type===\"load\"||/loaded|complete/.test(u.readyState)&&(!r.documentMode||r.documentMode<9))&&(n.clearTimeout(t.errorTimeout),n.clearTimeout(t.cssTimeout),u.onload=u.onreadystatechange=u.onerror=null,i())}function s(){if(t.state!==l&&t.cssRetries<=20){for(var i=0,f=r.styleSheets.length;i<f;i++)if(r.styleSheets[i].href===u.href){o({type:\"load\"});return}t.cssRetries++;t.cssTimeout=n.setTimeout(s,250)}}var u,h,f;i=i||w;h=at(t.url);h===\"css\"?(u=r.createElement(\"link\"),u.type=\"text/\"+(t.type||\"css\"),u.rel=\"stylesheet\",u.href=t.url,t.cssRetries=0,t.cssTimeout=n.setTimeout(s,500)):(u=r.createElement(\"script\"),u.type=\"text/\"+(t.type||\"javascript\"),u.src=t.url);u.onload=u.onreadystatechange=o;u.onerror=e;u.async=!1;u.defer=!1;t.errorTimeout=n.setTimeout(function(){e({type:\"timeout\"})},7e3);f=r.head||r.getElementsByTagName(\"head\")[0];f.insertBefore(u,f.lastChild)}function vt(){for(var t,u=r.getElementsByTagName(\"script\"),n=0,f=u.length;n<f;n++)if(t=u[n].getAttribute(\"data-headjs-load\"),!!t){i.load(t);return}}function yt(n,t){var v,p,e;return n===r?(o?f(t):d.push(t),i):(s(n)&&(t=n,n=\"ALL\"),a(n))?(v={},u(n,function(n){v[n]=c[n];i.ready(n,function(){y(v)&&f(t)})}),i):typeof n!=\"string\"||!s(t)?i:(p=c[n],p&&p.state===l||n===\"ALL\"&&y()&&o)?(f(t),i):(e=h[n],e?e.push(t):e=h[n]=[t],i)}function e(){if(!r.body){n.clearTimeout(i.readyTimeout);i.readyTimeout=n.setTimeout(e,50);return}o||(o=!0,vt(),u(d,function(n){f(n)}))}function k(){r.addEventListener?(r.removeEventListener(\"DOMContentLoaded\",k,!1),e()):r.readyState===\"complete\"&&(r.detachEvent(\"onreadystatechange\",k),e())}var r=n.document,d=[],h={},c={},ut=\"async\"in r.createElement(\"script\")||\"MozAppearance\"in r.documentElement.style||n.opera,o,g=n.head_conf&&n.head_conf.head||\"head\",i=n[g]=n[g]||function(){i.ready.apply(null,arguments)},nt=1,ft=2,tt=3,l=4,p;if(r.readyState===\"complete\")e();else if(r.addEventListener)r.addEventListener(\"DOMContentLoaded\",k,!1),n.addEventListener(\"load\",e,!1);else{r.attachEvent(\"onreadystatechange\",k);n.attachEvent(\"onload\",e);p=!1;try{p=!n.frameElement&&r.documentElement}catch(wt){}p&&p.doScroll&&function pt(){if(!o){try{p.doScroll(\"left\")}catch(t){n.clearTimeout(i.readyTimeout);i.readyTimeout=n.setTimeout(pt,50);return}e()}}()}i.load=i.js=ut?lt:ct;i.test=ot;i.ready=yt;i.ready(r,function(){y()&&u(h.ALL,function(n){f(n)});i.feature&&i.feature(\"domloaded\",!0)})})(window);
                        head.load(\"/lib/bootstrap/js/bootstrap.min.js\",
                                  \"/lib/jquery-custom-content-scroller/jquery.mCustomScrollbar.min.js\",
                                  \"/lib/bootstrap-select.min.js\",
                                  \"/lib/bootstrap3-typeahead.min.js\",
                                  \"/lib/prettiffy/prettify.js\",
                                  \"/js/reference.min.js?v={{commit|safe}}\");"]]))


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
      [:a.navbar-brand {:href "//anychart.com/"}
       [:img {:alt    "AnyChart"
              :height "72"
              :width  "300"
              :src    "/i/AnyChart-light-empty.png"}]
       [:div.chart-row
        [:span.chart-col.green]
        [:span.chart-col.orange]
        [:span.chart-col.red]]]
      [:a.brand.hidden-small-645 {:href "/"} " API Reference"]

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
    (head data)
    (body data)
    ;(resources/init-script data)
    (resources/init-script-fast data)
    (resources/google-tag-manager)))
