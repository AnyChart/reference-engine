(ns reference.handler
  (:require [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [org.httpkit.server :as server]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response redirect header]]
            [clostache.parser :refer [render-resource]]
            [taoensso.carmine.message-queue :as car-mq]
            [reference.data.versions :as v]
            [reference.adoc.core :as v-gen]
            [reference.data.pages :as p]
            [reference.config :as config]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [generate-string]])
  (:gen-class))

(if (System/getProperty "dev")
  (timbre/set-level! :info))

(defn- show-default-version [request]
  (redirect (str "/" (v/default-version) "/anychart")))

(defn- show-default-ns [request]
  (let [version (get-in request [:params :version])]
    (redirect (str "/" version "/anychart"))))

(defn- get-page-data [request]
  (let [version (get-in request [:params :version])
        page (get-in request [:params :page])]
    (if (and (v/version-exists? version)
             (p/page-exists? version page))
      (response {:content (p/get-page version page)
                 :info (p/page-info version page)
                 :version version
                 :page page}))))

(defn- show-page [request]
  (config/set-domain-from-request request)
  (let [version (get-in request [:params :version])
        page (get-in request [:params :page])]
    (if (and (v/version-exists? version)
             (p/page-exists? version page)) 
      (render-resource "templates/app.mustache"
                       {:version version
                        :debug false
                        :info (generate-string (p/page-info version page))
                        :page page
                        :versions (v/all-versions)
                        :static-version "11"
                        :content (p/get-page version page)
                        :link #(str "/" version "/" %)})
      (route/not-found (str "Not found: /" version "/" page)))))

(defn- get-tree-data [request]
  (let [version (get-in request [:params :version])]
    (-> (response (v/tree-json version))
        (header "Content-Type" "application/json"))))

(defn- get-search-data [request]
  (let [version (get-in request [:params :version])]
    (-> (response (v/search-index version))
        (header "Content-Type" "application/json"))))

(defn- try-show-page [request]
  (let [version (get-in request [:params :version])
        page (get-in request [:params :page])]
    (if (and (v/version-exists? version)
             (p/page-exists? version page))
      (redirect (str "/" version "/" page))
      (redirect (str "/" version "/anychart")))))

(defn- update-all [request]
  (config/set-domain-from-request request)
  (v/wcar* (car-mq/enqueue "reference-queue" "rebuild"))
  (str "ok!"))

(defroutes app-routes
  (route/resources "/")
  (GET "/" [] show-default-version)
  (GET "/_plz_" [] update-all)
  (POST "/_plz_" [] update-all)
  (GET "/:version/data/tree.json" [] get-tree-data)
  (GET "/:version/data/search.json" [] get-search-data)
  (GET "/:version" [] show-default-ns)
  (GET "/:version/" [] show-default-ns)
  (GET "/:version/try/:page" [] try-show-page)
  (GET "/:version/:page/data" [] get-page-data)
  (GET "/:version/:page" [] show-page)
  (route/not-found "not found"))

(def app (wrap-json-body
          (wrap-json-response (routes app-routes))
          {:keywords? true}))

(defn -main [domain & args]
  (def update-worker
    (car-mq/worker v/server-conn "reference-queue"
                   {:handler (fn [{:keys [message attempt]}]
                               (try
                                 (do
                                   (println message)
                                   (v-gen/build-all)
                                   {:status :success})
                                 (catch Exception e
                                   (do
                                     (println e)
                                     (v-gen/notify-slack "ШЕФ ВСЕ ПРОПАЛО")
                                     {:status :success}))))}))
  (if (seq args)
    (timbre/set-level! (keyword (first args))))
  (server/run-server #'app {:port 9197}))
