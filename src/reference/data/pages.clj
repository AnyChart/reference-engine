(ns reference.data.pages
  (:import [org.postgresql.util PGobject])
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [cheshire.core :refer [generate-string parse-string]]))

;; CREATE SEQUENCE page_id_seq;
;; CREATE TYPE page_type AS ENUM ('namespace', 'class', 'typedef', 'enum');
;; CREATE TABLE pages (
;;   id integer PRIMARY KEY DEFAULT nextval('page_id_seq'),
;;   type varchar(100),
;;   version_id integer references versions(id),
;;   url varchar(255) not null,
;;   full_name varchar(255),
;;   content jsonb
;; )

(defn pg-jsonb
  "Converts the given value to a PG JSONB object"
  [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(defn page-by-url [jdbc version-id page-url]
  (let [res (one jdbc (-> (select :*)
                          (from :pages)
                          (where [:= :version_id version-id]
                                 [:= :url page-url])))]
    (if (some? res)
      (assoc res :content (parse-string (-> res :content .getValue) true)))))

(defn page-and-version [jdbc version-key page-url]
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
    (if (some? res)
      (assoc res :content (parse-string (-> res :content .getValue) true)))))

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
