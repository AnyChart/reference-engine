(ns reference.data.pages
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [honeysql.core :as sql]
            [cheshire.core :refer [generate-string parse-string]]
            [mpg.util :as u]))

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

(defn page-by-url [jdbc version-id page-url]
  (let [res (one jdbc (-> (select :*)
                          (from :pages)
                          (where [:= :version_id version-id]
                                 [:= :url page-url])))]
    (if (some? res)
        (assoc res :content (parse-string (-> res :content .getValue) true)))))

(defn delete-version-pages [jdbc version-id]
  (exec jdbc (-> (delete-from :pages)
                 (where [:= :version_id version-id]))))

(defn add-page [jdbc version-id type url content]
  (insert! jdbc :pages {:url url
                        :type type
                        :content (u/pg-json content)
                        :full_name url
                        :version_id version-id}))

(defn page-exists? [jdbc version-id url]
  (not (nil? (one jdbc (-> (select :id)
                           (from :pages)
                           (where [:= :url url]
                                  [:= :version_id version-id]))))))

(defn info [page]
  {:full-name (:url page)
   :kind (:type page)})
