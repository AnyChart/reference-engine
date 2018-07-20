(ns reference.data.versions
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [version-clj.core :refer [version-compare]]
            [honeysql.helpers :refer :all]
            [cheshire.core :refer [generate-string parse-string]]
            [reference.data.sitemap :as sitemap]
            [reference.data.pages :as pdata]))


(defn add-version [jdbc key commit tree search show-samples]
  (first (insert! jdbc :versions {:key          key
                                  :commit       commit
                                  :show_samples show-samples
                                  :tree         (generate-string tree)
                                  :search       (generate-string search)})))


(defn version-by-key [jdbc key]
  (one jdbc (-> (select :key :id :show-samples)
                (from :versions)
                (where [:= :hidden false]
                       [:= :key key]))))


(defn version-tree-by-key [jdbc key]
  (one jdbc (-> (select :key :id :show-samples :tree)
                (from :versions)
                (where [:= :hidden false]
                       [:= :key key]))))


(defn version-by-id [jdbc version-id]
  (one jdbc (-> (select :key :id)
                (from :versions)
                (where [:= :hidden false]
                       [:= :id version-id]))))


(defn delete-by-key [jdbc key]
  (exec jdbc (-> (delete-from :versions)
                 (where [:= :key key]))))


(defn delete-by-id [jdbc id]
  (exec jdbc (-> (delete-from :versions)
                 (where [:= :id id]))))


(defn version-ids [jdbc key]
  (map :id (query jdbc (-> (select :id)
                           (from :versions)
                           (where [:= :key key])))))


(defn- version-keys [jdbc]
  (map :key (query jdbc (-> (select :key)
                            (from :versions)
                            (where [:= :hidden false])))))


(defn versions
  ([jdbc] (sort (comp - version-compare) (version-keys jdbc)))
  ([jdbc keys] (sort (comp - version-compare) (concat keys (version-keys jdbc)))))


(defn default
  ([jdbc] (first (versions jdbc)))
  ([jdbc keys] (first (versions jdbc keys))))


(defn versions-full-info [jdbc]
  (sort (comp - #(version-compare (:key %1) (:key %2)))
        (query jdbc (-> (select :id :key)
                        (from :versions)
                        (where [:= :hidden false])))))


(defn outdated-versions-ids [jdbc actual-ids]
  (if (seq actual-ids)
    (map :id (query jdbc (-> (select :id)
                             (from :versions)
                             (where [:not [:in :id actual-ids]]))))))


(defn remove-versions [jdbc ids]
  (if (seq ids)
    (exec jdbc (-> (delete-from :versions)
                   (where [:in :id ids])))))


(defn need-rebuild? [jdbc version-key commit]
  (nil? (one jdbc (-> (select :key)
                      (from :versions)
                      (where [:= :commit commit]
                             [:= :key version-key])))))


(defn search-index [jdbc version-id]
  (:search (one jdbc (-> (select :search)
                         (from :versions)
                         (where [:= :id version-id]
                                [:= :hidden false])))))


(defn tree-data [jdbc version-id]
  (:tree (one jdbc (-> (select :tree)
                       (from :versions)
                       (where [:= :id version-id]
                              [:= :hidden false])))))


(defn remove-branch-by-id [jdbc version-id]
  (pdata/delete-version-pages jdbc version-id)
  (sitemap/remove-by-version jdbc version-id)
  (delete-by-id jdbc version-id))


(defn remove-branch-by-key [jdbc branch-key]
  (let [version-id (:id (version-by-key jdbc branch-key))]
    (remove-branch-by-id jdbc version-id)))
