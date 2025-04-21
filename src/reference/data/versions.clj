(ns reference.data.versions
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [cheshire.core :refer [generate-string parse-string]]
            [reference.data.sitemap :as sitemap]
            [reference.data.pages :as pdata]
            [reference.util.utils :as utils]
            [taoensso.timbre :as timbre :refer [info error debug]]))


(defn add-version [jdbc key commit tree search show-samples]
  (debug "DEBUG: Adding version key:" key "commit:" commit "show_samples:" show-samples)
  (debug "DEBUG: Tree data type:" (type tree) "Tree empty?" (empty? tree) "Tree count:" (count tree))
  (try
    (let [tree-json (generate-string tree)
          _ (debug "DEBUG: Tree JSON length:" (count tree-json) "First 100 chars:" (subs tree-json 0 (min 100 (count tree-json))))
          result (first (insert! jdbc :versions {:key          key
                                    :commit       commit
                                    :show_samples show-samples
                                    :tree         tree-json
                                    :search       (generate-string search)}))]
      (debug "DEBUG: Version added successfully:" result)
      result)
    (catch Exception e
      (error e "Failed to add version")
      (debug "DEBUG: Exception details: " (.getMessage e))
      nil)))


(defn version-by-key [jdbc key]
  (info "DEBUG_DATA: version-by-key called with key:" key)
  (let [result (one jdbc (-> (select :key :id :show-samples)
                         (from :versions)
                         (where [:= :hidden false]
                                [:= :key key])))]
    (info "DEBUG_DATA: version-by-key result:" result)
    result))


(defn version-tree-by-key [jdbc key]
  (info "DEBUG_DATA: version-tree-by-key called with key:" key)
  (let [result (one jdbc (-> (select :key :id :show-samples :tree)
                         (from :versions)
                         (where [:= :hidden false]
                                [:= :key key])))]
    (info "DEBUG_DATA: version-tree-by-key result exists:" (boolean result))
    (when result
      (info "DEBUG_DATA: tree data exists:" (boolean (:tree result))))
    result))


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
  ([jdbc] (utils/sort-versions (version-keys jdbc)))
  ([jdbc keys] (utils/sort-versions (concat keys (version-keys jdbc)))))


(defn default
  ([jdbc] (first (versions jdbc)))
  ([jdbc keys] (first (versions jdbc keys))))


(defn versions-full-info [jdbc]
  (->> (query jdbc (-> (select :id :key)
                       (from :versions)
                       (where [:= :hidden false])))
       (utils/sort-versions :key)))


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
  (debug "DEBUG: Fetching tree data for version ID:" version-id)
  (let [result (:tree (one jdbc (-> (select :tree)
                       (from :versions)
                       (where [:= :id version-id]
                              [:= :hidden false]))))]
    (debug "DEBUG: Tree data exists?" (boolean result) "Tree data length:" (when result (count result)))
    result))


(defn remove-branch-by-id [jdbc version-id]
  (pdata/delete-version-pages jdbc version-id)
  (sitemap/remove-by-version jdbc version-id)
  (delete-by-id jdbc version-id))


(defn remove-branch-by-key [jdbc branch-key]
  (let [version-id (:id (version-by-key jdbc branch-key))]
    (remove-branch-by-id jdbc version-id)))