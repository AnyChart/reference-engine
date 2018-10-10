(ns reference.web.routes
  (:require
    ;; components
    [reference.components.redis :as redisca]
    ;; data
    [reference.data.versions :as vdata]
    [reference.data.pages :as pdata]
    [reference.data.seo :as seo]
    [reference.web.data :as wdata]
    [reference.web.helpers :refer :all]
    ;; hanlers
    [reference.web.handlers.admin-handlers :as admin-handlers]
    [reference.web.handlers.sitemap-handlers :as sitemap-handlers]
    [reference.web.handlers.search-handlers :as search-handlers]
    [reference.web.handlers.versions-handlers :as versions-handlers]
    [reference.web.handlers.handlers-404 :as handlers-404]
    ;; views
    [reference.web.views.landing.landing-content :as landing-content-view]
    [reference.web.views.main.main-page :as main-page-view]
    ;; others
    [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
    [compojure.core :refer [routes defroutes GET POST ANY]]
    [compojure.route :as route]
    [ring.util.response :refer [response redirect header content-type]]
    [selmer.parser :refer [render-file]]
    [taoensso.timbre :as timbre :refer [info]]
    [cheshire.core :refer [generate-string]]
    [hiccup.core :as hiccup]))


(defn- prefix-title [page]
  (str (:url page) " " (:type page)))


(defn- get-page-title [prefix & [version]]
  (str prefix " | AnyChart API Reference"
       (when version (str " v" (:key version)))))


(defn- get-page-description [page is-url-version version]
  (str (:url page) " " (:type page)
       (when (seq (:short-description (:content page)))
         (str " | " (:short-description (:content page))))
       " | AnyChart API Reference" (when is-url-version (str " v" (:key version)))))


(defn- landing-content [request]
  (let [versions (vdata/versions (jdbc request))
        version-key (first versions)]
    (response {:content     (hiccup/html (landing-content-view/landing-data {:versions versions :version version-key}))
               :info        {}
               :keywords    "anychart api reference, js charts, javascript charts, html5 charts, ajax charts, plots, line charts, bar charts, pie charts, js maps, javascript gantt charts, js dashboard"
               :description "AnyChart HTML5 charts for for web and mobile - API reference"
               :version     version-key
               :page        ""
               :url         "https://api.anychart.com/"
               :title       "AnyChart API Reference"})))


(defn- show-landing [request]
  (let [versions (vdata/versions (jdbc request))
        version (vdata/version-tree-by-key (jdbc request) (first versions))
        data {:version        (:key version)
              :tree           (:tree version)
              :url            "https://api.anychart.com/"
              :is-last        true
              :last-version   (first versions)
              :versions       versions
              :debug          false
              :info           (generate-string {})
              :page           ""
              :static-version "12"
              :footer         (seo/random-entry)
              :content        (hiccup/html (landing-content-view/landing-data {:versions versions :version (:key version)}))
              :link           #(str "/" (:key version) "/" %)
              :title          "AnyChart API Reference"
              :is-url-version false
              :commit         (:commit (config request))}]
    ;(println (:commit (config request)))
    ;(render-file "templates/app.selmer" data)
    (main-page-view/page data)
    ))


(defn- redirect-latest [request]
  (redirect (if (-> request :params :entry)
              (str "/?" (:query-string request))
              "/anychart")))


(defn- redirect-latest-page [request]
  (let [page (get-in request [:route-params :page])]
    (redirect (str "/" page))))


(defn- tree-data [version is-url-version versions request]
  (-> (response (vdata/tree-data (jdbc request) (:id version)))
      (header "Content-Type" "application/json")))


(defn- generate-page-content [version is-url-version page request]
  (if-let [cached-data nil]                                 ;;(redisca/cached-data (redis request) (:id version) (:url page))]
    cached-data
    (let [data (wdata/render-entry (get-in request [:component :config :docs])
                                   (get-in request [:component :config :playground])
                                   (:key version)
                                   (:show_samples version)
                                   (:type page)
                                   (:content page)
                                   is-url-version)]
      ;(redisca/cache (redis request) (:id version) (:url page) data)
      data)))


(defn- get-page-data [version is-url-version page request]
  (let [info (pdata/info page)]
    (response {:content     (generate-page-content version is-url-version page request)
               :info        info
               :version     (:key version)
               :page        (:url page)
               :keywords    (str (:url page) " " (:kind info) ", anychart api reference, js charts, javascript charts, html5 charts, ajax charts, plots, line charts, bar charts, pie charts, js maps, javascript gantt charts, js dashboard")
               :description (str "AnyChart HTML5 charts for for web and mobile - API reference for " (:url page) " " (:kind info))
               :title       (get-page-title (prefix-title page) version)
               :url         (str "https://api.anychart.com/" (:key version) "/" (:url page))})))


(defn- show-page [version is-url-version versions request]
  (let [version-key (:key version)
        page-url (-> request :route-params :page)]
    (when-let [page (pdata/page-and-version (jdbc request) version-key page-url)]
      (let [data {:version        version-key
                  :tree           (:tree page)
                  :is-last        (= version-key (first versions))
                  :last-version   (first versions)
                  :versions       versions
                  :url            (str "https://api.anychart.com/" (:url page))
                  :debug          false
                  :info           (generate-string (pdata/info page))
                  :page           (:url page)
                  :page-name      (str (:url page) " " (:type page))
                  :footer         (seo/random-entry)
                  :static-version "12"
                  :content        (generate-page-content version is-url-version page request)
                  :link           #(str "/" version-key "/" %)
                  :title          (get-page-title (prefix-title page) (when is-url-version version))
                  :description    (get-page-description page is-url-version version)
                  :is-url-version is-url-version
                  :commit         (:commit (config request))}]
        ;(render-file "templates/app.selmer" data)
        (main-page-view/page data)
        ))))


(defn- show-default-ns [version is-url-version versions request]
  (if-let [search (-> request :params :entry)]
    (let [versions (vdata/versions (jdbc request))
          data {:version        (:key version)
                :tree           (vdata/tree-data (jdbc request) (:id version))
                :is-last        (= (:key version) (first versions))
                :last-version   (first versions)
                :versions       versions
                :url            (str "https://api.anychart.com/" (:key version) "/?entry=" search)
                :debug          false
                :page           (str "/?entry=" search)
                :page-name      (str "Search results for " search)
                :footer         (seo/random-entry)
                :static-version "12"
                :content        ""
                :link           #(str "/" (:key version) "/" %)
                :title          (get-page-title "Search results" version)
                :is-url-version is-url-version
                :commit         (:commit (config request))}]
      ;(render-file "templates/app.selmer" data)
      (main-page-view/page data)
      )
    (redirect (str "/" (:key version) "/anychart"))))


(defn- try-show-page [version is-url-version versions request]
  (let [page-url (get-in request [:route-params :page])]
    (if (pdata/page-exists? (jdbc request) (:id version) page-url)
      (redirect (str
                  (when is-url-version (str "/" (:key version)))
                  "/" page-url))
      (show-default-ns version is-url-version versions request))))


(defn- check-version-middleware [app]
  (fn [request]
    (let [is-url-version (boolean (-> request :route-params :version))
          versions (vdata/versions (jdbc request))
          version-key (or (-> request :route-params :version) (first versions))
          version (vdata/version-by-key (jdbc request) version-key)]
      (when version
        (app version is-url-version versions request)))))


(defn- check-page-middleware [app]
  (fn [version is-url-version versions request]
    (let [page-url (-> request :route-params :page)
          page (pdata/page-by-url (jdbc request) (:id version) page-url)]
      (when (and version page)
        (app version is-url-version page request)))))


(defroutes app-routes
           (route/resources "/")
           (GET "/" [] show-landing)

           ;; admin
           (GET "/_update_reference_" [] admin-handlers/update-versions)
           (POST "/_update_reference_" [] admin-handlers/update-versions)
           (GET "/_admin_" [] admin-handlers/admin-panel)
           (POST "/_delete_" [] admin-handlers/delete-version)
           (POST "/_rebuild_" [] admin-handlers/rebuild-version)

           ;; sitemap
           (GET "/sitemap" [] sitemap-handlers/show-sitemap)
           (GET "/sitemap.xml" [] sitemap-handlers/show-sitemap)

           (GET "/versions" [] versions-handlers/list-versions)
           (GET "/latest/" [] redirect-latest)
           (GET "/latest" [] redirect-latest)
           (GET "/latest/:page" [] redirect-latest-page)

           (GET "/:page" [] (check-version-middleware show-page))
           (GET "/:page/data" [] (check-version-middleware
                                   (check-page-middleware
                                     get-page-data)))
           (GET "/try/:page" [] (check-version-middleware try-show-page))

           (GET "/:version/landing/data" [] landing-content)
           (GET "/:version/" [] (check-version-middleware show-default-ns))
           (GET "/:version" [] (check-version-middleware show-default-ns))
           (GET "/:version/data/tree.json" [] (check-version-middleware tree-data))
           (GET "/:version/data/search.json" [] (check-version-middleware search-handlers/search-data))
           (GET "/:version/search.json" [] (check-version-middleware search-handlers/search))
           (GET "/:version/try/:page" [] (check-version-middleware try-show-page))
           (GET "/:version/:page/data" [] (check-version-middleware
                                            (check-page-middleware
                                              get-page-data)))
           (GET "/:version/:page" [] (check-version-middleware show-page))
           (route/not-found handlers-404/error-404))
