(ns reference.data.versions
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [version-clj.core :refer [version-compare]]))

;; CREATE SEQUENCE version_id_seq;
;; CREATE TABLE versions (
;;    id integer PRIMARY KEY DEFAULT nextval('version_id_seq'),
;;    key varchar(255) not NULL,
;;    commit varchar(40) not NULL,
;;    hidden BOOLEAN DEFAULT FALSE,
;;    tree TEXT,
;;    search TEXT
;; );

(defn add-version [jdbc key commit tree search]
  (:id (first (insert! jdbc :versions {:key key
                                       :commit commit
                                       :tree tree
                                       :search search}))))

(defn version-by-key [jdbc key]
  (one jdbc (-> (select :key :id)
                (from :versions)
                (where [:= :hidden false]
                       [:= :key key]))))

(defn version-by-id [jdbc version-id]
  (one jdbc (-> (select :key :id)
                (from :versions)
                (where [:= :hidden false]
                       [:= :id version-id]))))

(defn versions [jdbc project-id]
  (reverse
   (sort version-compare
         (map :key (query jdbc (-> (select :key)
                                   (from :versions)
                                   (where [:= :hidden false])))))))

(defn outdated-versions-ids [jdbc actual-ids]
  (map :id (query jdbc (-> (select :id)
                           (from :versions)
                           (where [:not [:in :id actual-ids]])))))

(defn remove-versions [jdbc ids]
  (if (seq ids)
    (exec jdbc (-> (delete-from :versions)
                   (where [:in :id ids])))))

(defn default [jdbc]
  (first (versions jdbc)))


(defn need-rebuild? [jdbc version-key commit]
  (nil? (one jdbc (-> (select :key)
                      (from :versions)
                      (where [:= :commit commit]
                             [:= :key version-key])))))
