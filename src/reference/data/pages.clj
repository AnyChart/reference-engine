(ns reference.data.pages
  (:import [org.postgresql.util PGobject])
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [cheshire.core :refer [generate-string parse-string]]
            [taoensso.timbre :as timbre :refer [error debug]]))


(defn pg-jsonb
  "Converts the given value to a PG JSONB object"
  [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(defn page-by-url [jdbc version-id page-url]
  (timbre/info "DEBUG: page-by-url called with version-id:" version-id "page-url:" page-url)
  (let [res (one jdbc (-> (select :*)
                          (from :pages)
                          (where [:= :version_id version-id]
                                 [:= :url page-url])))]
    (timbre/info "DEBUG: page-by-url query result:" (boolean res))
    (if (some? res)
      (let [parsed-content (parse-string (-> res :content .getValue) true)
            result (assoc res :content parsed-content)]
        (timbre/info "DEBUG: page-by-url parsed content exists:" (boolean parsed-content))
        result)
      (do
        (timbre/info "DEBUG: page-by-url returned nil")
        nil))))

(defn page-and-version [jdbc version-key page-url]
  (timbre/info "DEBUG: page-and-version called with version-key:" version-key "page-url:" page-url)
  (let [res (one jdbc (-> (select :pages.*
                                  [:versions.id "version-id"]
                                  [:versions.key "version-key"]
                                  [:versions.tree "tree"]
                                  [:versions.show-samples "show-samples"])
                          (from :pages :versions)
                          (where [:and
                                  [:= :versions.id :pages.version_id]
                                  [:= :versions.key version-key]
                                  [:= :pages.url page-url]])))]
    (timbre/info "DEBUG: page-and-version query result:" (boolean res))
    (if (some? res)
      (let [parsed-content (parse-string (-> res :content .getValue) true)
            result (assoc res :content parsed-content)]
        (timbre/info "DEBUG: page-and-version parsed content exists:" (boolean parsed-content))
        result)
      (do
        (timbre/info "DEBUG: page-and-version returned nil")
        nil))))

(defn delete-version-pages [jdbc version-id]
  (exec jdbc (-> (delete-from :pages)
                 (where [:= :version_id version-id]))))

(defn add-page [jdbc version-id type url content]
  (insert! jdbc :pages {:url        url
                        :type       type
                        :content    (pg-jsonb content)
                        :full_name  url
                        :version_id version-id}))

(defn page-exists? [jdbc version-id url]
  (not (nil? (one jdbc (-> (select :id)
                           (from :pages)
                           (where [:= :url url]
                                  [:= :version_id version-id]))))))

(defn info [page]
  {:full-name (:url page)
   :kind      (:type page)})
