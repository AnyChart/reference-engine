(ns reference.web.routes
  (:require [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response redirect header]]
            [selmer.parser :refer [render-file]]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [reference.components.redis :as redisca]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [generate-string]]
            [reference.web.data :as wdata]))

(defn- static-version [request]
  (-> request :component :config :static))

(defn- jdbc [request]
  (-> request :component :jdbc))

(defn- redis [request]
  (-> request :component :redis))

(defn- landing-content [request]
  (let [versions (vdata/versions (jdbc request))
        version-key (first versions)]
        (response {:content (render-file "templates/landing-content.selmer"
                                         {:versions versions})
                   :info {}
                   :version version-key
                   :page ""
                   :title "AnyChart API Reference"})))

(defn- show-landing [request]
  (let [versions (vdata/versions (jdbc request))
        version (vdata/version-by-key (jdbc request) (first versions))]
    (render-file "templates/app.selmer"
                 {:version (:key version)
                  :tree (vdata/tree-data (jdbc request) (:id version))
                  :is-last true
                  :last-version (first versions)
                  :versions versions
                  :debug false
                  :info (generate-string {})
                  :page ""
                  :static-version "12"
                  :content (render-file "templates/landing-content.selmer"
                                        {:versions versions})
                  :link #(str "/" (:key version) "/" %)
                  :title "AnyChart API Reference"})))

(defn- show-default-version [request]
  (redirect (str "/" (vdata/default (jdbc request)) "/anychart")))

(defn- show-default-ns [version request]
  (redirect (str "/" (:key version) "/anychart")))

(defn- redirect-latest [request]
  (redirect (str "/" (vdata/default (jdbc request)) "/anychart")))

(defn- redirect-latest-page [request]
  (let [page (get-in request [:route-params :page])]
    (redirect (str "/" (vdata/default (jdbc request)) "/" page))))

(defn- search-data [version request]
  (-> (response (vdata/search-index (jdbc request) (:id version)))
      (header "Content-Type" "application/json")))

(defn- tree-data [version request]
  (-> (response (vdata/tree-data (jdbc request) (:id version)))
      (header "Content-Type" "application/json")))

(defn- try-show-page [version request]
  (let [page-url (get-in request [:route-params :page])]
    (if (pdata/page-exists? (jdbc request) (:id version) page-url)
      (redirect (str "/" (:key version) "/" page-url))
      (show-default-ns version request))))

(defn- generate-page-content [version page request]
  (if-let [cached-data nil];;(redisca/cached-data (redis request) (:id version) (:url page))]
    cached-data
    (let [data (wdata/render-entry (get-in request [:component :config :docs])
                                   (get-in request [:component :config :playground])
                                   (:key version)
                                   (:show_samples version)
                                   (:type page)
                                   (:content page))]
      (redisca/cache (redis request) (:id version) (:url page) data)
      data)))

(defn- get-page-title [version page info]
  (str (:url page) " " (:kind info) " | version " (:key version) " | AnyChart API Reference"))

(defn- get-page-data [version page request]
  (let [info (pdata/info page)]
    (response {:content (generate-page-content version page request)
               :info info
               :version (:key version)
               :page (:url page)
               :title (get-page-title version page info)})))

(defn- show-page [version page request]
  (let [versions (vdata/versions (jdbc request))
        info (pdata/info page)]
    (render-file "templates/app.selmer"
                 {:version (:key version)
                  :tree (vdata/tree-data (jdbc request) (:id version))
                  :is-last (= (:key version) (first versions))
                  :last-version (first versions)
                  :versions versions
                  :debug false
                  :info (generate-string info)
                  :page (:url page)
                  :static-version "12"
                  :content (generate-page-content version page request)
                  :link #(str "/" (:key version) "/" %)
                  :title (get-page-title version page info)})))

(defn- request-update [request]
  (redisca/enqueue (redis request)
                   (-> request :component :config :reference-queue)
                   "generate"))

(defn- check-version-middleware [app]
  (fn [request]
    (let [version-key (-> request :route-params :version)
          version (vdata/version-by-key (jdbc request) version-key)]
      (if version
        (app version request)
        (route/not-found "version not found")))))

(defn- check-page-middleware [app]
  (fn [request]
    (let [version-key (-> request :route-params :version)
          page-url (-> request :route-params :page)
          version (vdata/version-by-key (jdbc request) version-key)
          page (pdata/page-by-url (jdbc request) (:id version) page-url)]
      (if (and version page)
        (app version page request)
        (route/not-found "page not found")))))

(defroutes app-routes
  (route/resources "/")
  (GET "/" [] show-landing)
  (GET "/_update_reference_" [] request-update)
  (POST "/_update_reference_" [] request-update)
  (GET "/latest/" [] redirect-latest)
  (GET "/latest" [] redirect-latest)
  (GET "/latest/:page" [] redirect-latest-page)
  (GET "/:version/landing/data" [] landing-content)
  (GET "/:version/" [] (check-version-middleware show-default-ns))
  (GET "/:version" [] (check-version-middleware show-default-ns))
  (GET "/:version/data/tree.json" [] (wrap-json-response (check-version-middleware
                                                          tree-data)))
  (GET "/:version/data/search.json" [] (wrap-json-response (check-version-middleware
                                                            search-data)))
  (GET "/:version/try/:page" [] (check-version-middleware try-show-page))
  (GET "/:version/:page/data" [] (wrap-json-response (check-page-middleware get-page-data)))
  (GET "/latest/:page" [] redirect-latest-page)
  (GET "/:version/:page" [] (check-page-middleware show-page)))

(def app (routes app-routes))
