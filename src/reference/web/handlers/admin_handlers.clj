(ns reference.web.handlers.admin-handlers
  (:require [reference.web.helpers :refer :all]
            [reference.data.versions :as vdata]
            [reference.web.views.admin-page :as admin-view]
            [taoensso.timbre :as timbre]
            [reference.components.redis :as redisca]))


(defn admin-panel [request]
  (let [versions (vdata/versions-full-info (jdbc request))]
    (admin-view/page versions)))


(defn delete-version [request]
  (let [version-key (-> request :params :version)]
    (timbre/info "DELETE version request:" version-key)
    (vdata/remove-branch-by-key (jdbc request) version-key)))


(defn rebuild-version [request]
  (let [params (-> request :params)]
    (timbre/info "REBUILD version request:" params)
    ;; just for not showing updated version in select on admin panel
    (when-let [version (:version params)]
      (vdata/remove-branch-by-key (jdbc request) version))
    (redisca/enqueue (redis request)
                     (-> request :component :config :reference-queue)
                     (assoc params :cmd "generate"))))