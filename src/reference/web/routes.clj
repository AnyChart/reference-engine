(ns reference.web.routes
  (:require [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response redirect header]]
            [clostache.parser :refer [render-resource]]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [generate-string]]))

(defn- static-version [request]
  (-> request :component :config :static))

(defn- jdbc [request]
  (-> request :component :jdbc))

(defn- redis [request]
  (-> request :component :redis))

(defn- show-default-version [request]
  (redirect (str "/" (vdata/default (jdbc request)) "/anychart")))

(defn- show-default-ns [version request]
  (redirect (str "/" (:key version) "/anychart")))

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

(defn- get-page-data [version page request]
  (response {:content (:content page)
             :info (pdata/info page)
             :version (:key version)
             :page (:url page)}))

(defn- show-page [version page request]
  (render-resource "templates/app.mustache"
                       {:version (:key version)
                        :debug false
                        :info (generate-string (pdata/info page))
                        :page (:url page)
                        :versions (vdata/versions (jdbc request))
                        :static-version "12"
                        :content (:content page)
                        :link #(str "/" (:key version) "/" %)}))

(defn- request-update [request])

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
  (GET "/" [] show-default-version)
  (GET "/_update_" [] request-update)
  (POST "/_update_" [] request-update)
  (GET "/:version/" [] (check-version-middleware show-default-ns))
  (GET "/:version" [] (check-version-middleware show-default-ns))
  (GET "/:version/data/tree.json" [] (wrap-json-response (check-version-middleware
                                                          tree-data)))
  (GET "/:version/data/search.json" [] (wrap-json-response (check-version-middleware
                                                            search-data)))
  (GET "/:version/try/:page" [] (check-version-middleware try-show-page))
  (GET "/:version/:page/data" [] (wrap-json-response (check-page-middleware get-page-data)))
  (GET "/:version/:page" [] (check-page-middleware show-page)))

(def app (routes app-routes))
