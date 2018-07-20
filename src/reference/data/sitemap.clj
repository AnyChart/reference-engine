(ns reference.data.sitemap
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [honeysql.core :as sql]))


(defn remove-by-version [jdbc version-id]
  (exec jdbc (-> (delete-from :sitemap)
                 (where [:= :version_id version-id]))))


(defn- create-sitemap-entry [entry version-id]
  {:page_url      (:full-name entry)
   :version_id    version-id
   :last_modified (:last-modified entry)})


(defn update-sitemap [jdbc version-id entries]
  (let [entries (concat (map #(create-sitemap-entry % version-id) (:namespaces entries))
                        (map #(create-sitemap-entry % version-id) (:classes entries))
                        (map #(create-sitemap-entry % version-id) (:enums entries))
                        (map #(create-sitemap-entry % version-id) (:typedefs entries)))]
    (if (seq entries)
      (exec jdbc (-> (insert-into :sitemap)
                     (values entries))))))


(defn version-entries [jdbc version-id]
  (query jdbc (-> (select :page_url :last_modified)
                  (from :sitemap)
                  (where [:= :version_id version-id]))))