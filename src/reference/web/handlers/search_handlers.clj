(ns reference.web.handlers.search-handlers
  (:require [reference.web.helpers :refer :all]
            [reference.data.versions :as vdata]
            [reference.data.search :as search-data]
            [ring.util.response :refer [response redirect header content-type]]))


(defn search-data [version is-url-version versions request]
  (-> (response (vdata/search-index (jdbc request) (:id version)))
      (header "Content-Type" "application/json")))


(defn search [version is-url-version versions request]
  (let [q (-> request :params :q)
        data (response (search-data/search (jdbc request) (:id version) q))]
    (-> data (header "Content-Type" "application/json"))))