(ns reference.data.pages
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [honeysql.core :as sql]))

;; CREATE SEQUENCE page_id_seq;
;; CREATE TYPE page_type AS ENUM ('namespace', 'class', 'typedef', 'enum');
;; CREATE TABLE pages (
;;   id integer PRIMARY KEY DEFAULT nextval('page_id_seq'),
;;   type varchar(100),
;;   version_id integer references versions(id),
;;   url varchar(255) not null,
;;   content text
;; )

(defn page-by-url [jdbc version-id page-url]
  (one jdbc (-> (select :*)
                (from :pages)
                (where [:= :version_id version-id]
                       [:= :url page-url]))))

(defn add-page [jdbc version-id type url content]
  (insert! jdbc :pages {:url url
                        :type type
                        :content content
                        :version_id version-id}))
