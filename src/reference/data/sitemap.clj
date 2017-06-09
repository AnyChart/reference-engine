(ns reference.data.sitemap
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [reference.data.versions :as vdata]
            [honeysql.helpers :refer :all]
            [honeysql.core :as sql]
            [clojure.xml :refer [emit emit-element]]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]))

;; create table sitemap (
;;  page_url varchar(255),
;;  version_id integer references versions(id),
;;  last_modified bigint
;; );

(def formatter (f/formatter "YYYY-MM-dd'T'hh:mm:ss'Z'"))

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

(defn- version-entries [jdbc version-id]
  (query jdbc (-> (select :page_url :last_modified)
                  (from :sitemap)
                  (where [:= :version_id version-id]))))

(defn- get-priority [idx]
  (cond
    (> idx 5) 0.1
    :else (- 0.6 (/ idx 10))))

(defn- generate-version-sitemap [jdbc idx version]
  (let [priority (get-priority idx)
        entries (version-entries jdbc (:id version))]
    (map (fn [entry]
           {:tag     :url
            :content [{:tag :loc :content
                            [(str "https://api.anychart.com/" (:page_url entry))]}
                      {:tag :priority :content [(format "%.1f" priority)]}
                      {:tag :changefreq :content ["monthly"]}
                      {:tag :lastmod :content [(f/unparse formatter
                                                          (c/from-long (* 1000 (:last_modified entry))))]}]})
         entries)))

(defn- landing-entry []
  {:tag     :url
   :content [{:tag :loc :content ["https://api.anychart.com/"]}
             {:tag :lastmod :content ["2017-01-01T00:00:00Z"]}
             {:tag :priority :content ["0.6"]}
             {:tag :changefreq :content ["monthly"]}]})

(defn generate-sitemap [jdbc]
  (with-out-str
    (emit {:tag     :urlset :attrs {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
           :content (conj (apply concat (map-indexed (fn [idx val]
                                                       (generate-version-sitemap jdbc idx val))
                                                     (vdata/versions-full-info jdbc)))
                          (landing-entry))})))
