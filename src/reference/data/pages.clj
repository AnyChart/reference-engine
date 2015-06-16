(ns reference.data.pages
  (:require [reference.components.jdbc :refer [query one insert! exec]]))

;; CREATE SEQUENCE page_id_seq;
;; CREATE TYPE page_type AS ENUM ('namespace', 'class', 'typedef', 'enum');
;; CREATE TABLE pages (
;;   id integer PRIMARY KEY DEFAULT nextval('page_id_seq'),
;;   type page_type,
;;   version_id integer references versions(id),
;;   url varchar(255) not null,
;;   content text
;; )

(defn page-by-url [version-id page-url]
  (one jdbc (-> (select :*)
                (from :samples)
                (where [:= :version_id version-id]
                       [:= :url url]))))

(defn add-page [version-id type url content]
  (insert! jdbc :pages {:url url
                        :type type
                        :content content
                        :version_id version-id}))
