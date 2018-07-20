(ns reference.web.routes
  (:require [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [routes defroutes GET POST ANY]]
            [compojure.route :as route]
            [ring.util.request :refer [request-url]]
            [ring.util.response :refer [response redirect header content-type]]
            [selmer.parser :refer [render-file]]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [reference.data.search :as search-data]
            [reference.data.sitemap :as sdata]
            [reference.data.seo :as seo]
            [reference.components.redis :as redisca]
            [reference.components.notifier :refer [notify-404]]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [generate-string]]
            [reference.web.data :as wdata]))


(defn- config [request]
  (-> request :component :config))

(defn- static-version [request]
  (-> request :component :config :static))

(defn- jdbc [request]
  (-> request :component :jdbc))

(defn- redis [request]
  (-> request :component :redis))

(defn- notifier [request]
  (-> request :component :notifier))


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


(defn- page-404 [request]
  (let [referrer (get-in request [:headers "referer"])
        ua (get-in request [:headers "user-agent"])]
    (when (not (.contains ua "Slackbot"))
      (if referrer
        (notify-404 (notifier request) (str (request-url request) " from " referrer))
        (notify-404 (notifier request) (request-url request)))))
  (route/not-found "page not found"))


(defn- landing-content [request]
  (let [versions (vdata/versions (jdbc request))
        version-key (first versions)]
    (response {:content     (render-file "templates/landing-content.selmer"
                                         {:versions versions
                                          :version  version-key})
               :info        {}
               :keywords    "anychart api reference, js charts, javascript charts, html5 charts, ajax charts, plots, line charts, bar charts, pie charts, js maps, javascript gantt charts, js dashboard"
               :description "AnyChart HTML5 charts for for web and mobile - API reference"
               :version     version-key
               :page        ""
               :url         "https://api.anychart.com/"
               :title       "AnyChart API Reference"})))


(defn- show-sitemap [request]
  (-> (response (sdata/generate-sitemap (jdbc request)))
      (content-type "text/xml")))


(defn- show-landing [request]
  (let [versions (vdata/versions (jdbc request))
        version (vdata/version-tree-by-key (jdbc request) (first versions))]
    (println (:commit (config request)))
    (render-file "templates/app.selmer"
                 {:version        (:key version)
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
                  :content        (render-file "templates/landing-content.selmer"
                                               {:versions versions
                                                :version  (:key version)})
                  :link           #(str "/" (:key version) "/" %)
                  :title          "AnyChart API Reference"
                  :is-url-version false
                  :commit         (:commit (config request))})))


(defn- redirect-latest [request]
  (redirect (if (-> request :params :entry)
              (str "/?" (:query-string request))
              "/anychart")))


(defn- redirect-latest-page [request]
  (let [page (get-in request [:route-params :page])]
    (redirect (str "/" page))))


(defn- search-data [version is-url-version versions request]
  (-> (response (vdata/search-index (jdbc request) (:id version)))
      (header "Content-Type" "application/json")))


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
      (render-file "templates/app.selmer"
                   {:version        version-key
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
                    :commit         (:commit (config request))}))))


(defn- show-default-ns [version is-url-version versions request]
  (if-let [search (-> request :params :entry)]
    (let [versions (vdata/versions (jdbc request))]
      (render-file "templates/app.selmer"
                   {:version        (:key version)
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
                    :commit         (:commit (config request))}))
    (redirect (str "/" (:key version) "/anychart"))))


(defn- try-show-page [version is-url-version versions request]
  (let [page-url (get-in request [:route-params :page])]
    (if (pdata/page-exists? (jdbc request) (:id version) page-url)
      (redirect (str
                  (when is-url-version (str "/" (:key version)))
                  "/" page-url))
      (show-default-ns version is-url-version versions request))))


(defn- list-versions [request]
  (response (vdata/versions (jdbc request))))


(defn- request-update [request]
  (redisca/enqueue (redis request)
                   (-> request :component :config :reference-queue)
                   "generate"))


(defn- search [version is-url-version versions request]
  (let [q (-> request :params :q)
        data (response (search-data/search (jdbc request) (:id version) q))]
    (-> data (header "Content-Type" "application/json"))))


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
           (GET "/sitemap" [] show-sitemap)
           (GET "/sitemap.xml" [] show-sitemap)
           (GET "/_update_reference_" [] request-update)
           (POST "/_update_reference_" [] request-update)

           (GET "/versions" [] list-versions)
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
           (GET "/:version/data/search.json" [] (check-version-middleware search-data))
           (GET "/:version/search.json" [] (check-version-middleware search))
           (GET "/:version/try/:page" [] (check-version-middleware try-show-page))
           (GET "/:version/:page/data" [] (check-version-middleware
                                            (check-page-middleware
                                              get-page-data)))
           (GET "/:version/:page" [] (check-version-middleware show-page))
           (route/not-found page-404))
