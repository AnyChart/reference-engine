(ns reference.data.sitemap
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [reference.data.versions :as vdata]
            [honeysql.helpers :refer :all]
            [honeysql.core :as sql]
            [clojure.xml :refer [emit]]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

;; create table sitemap (
;;  page_url varchar(255),
;;  version_id integer references versions(id),
;;  last_modified bigint
;; );

(defn remove-by-version [jdbc version-id]
  (exec jdbc (-> (delete-from :sitemap)
                 (where [:= :version_id version-id]))))

(defn- create-sitemap-entry [entry version-id]
  {:page_url (:full-name entry)
   :version_id version-id
   :last_modified (:last-modified entry)})

(defn update-sitemap [jdbc version-id entries]
  (let [entries (concat (map #(create-sitemap-entry % version-id) (:namespaces entries))
                        (map #(create-sitemap-entry % version-id) (:classes entries))
                        (map #(create-sitemap-entry % version-id) (:enums entries))
                        (map #(create-sitemap-entry % version-id) (:typedefs entries)))]
    (exec jdbc (-> (insert-into :sitemap)
                   (values entries)))))

(defn- version-entries [jdbc version-id]
  (query jdbc (-> (select :page_url :last_modified)
                  (from :sitemap)
                  (where [:= :version_id version-id]))))

(defn- generate-version-sitemap [jdbc idx version]
  (let [priority (cond
                   (< idx 5) 0.1
                   :else (- 0.6 (/ idx 10)))]
    (map (fn [entry]
           {:tag :url
            :content [{:tag :loc :content
                       [(str "https://api.anychart.com/" (:key version) "/"
                             (:page_url entry))]}
                      {:tag :priority :content [priority]}
                      {:tag :lastmod :content [c/from-long (:last_modified entry)]}]})
         (version-entries jdbc (:id version)))))

(defn generate-sitemap [jdbc]
  (emit {:tag :urlset :attrs {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
         :content (apply concat (map-indexed (fn [idx val]
                                               (generate-version-sitemap jdbc idx val))
                                             vdata/versions-full-info))}))
