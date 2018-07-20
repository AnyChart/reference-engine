(ns reference.web.handlers.sitemap-handlers
  (:require [reference.data.sitemap :as sdata]
            [reference.web.helpers :refer :all]
            [ring.util.response :refer [response redirect header content-type]]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.xml :refer [emit emit-element]]
            [reference.data.versions :as vdata]))


(def formatter (f/formatter "YYYY-MM-dd'T'hh:mm:ss'Z'"))


(defn- get-priority [idx]
  (cond
    (> idx 5) 0.1
    :else (- 0.6 (/ idx 10))))


(defn- generate-version-sitemap [jdbc idx version]
  (let [priority (get-priority idx)
        entries (sdata/version-entries jdbc (:id version))]
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


(defn show-sitemap [request]
  (-> (response (generate-sitemap (jdbc request)))
      (content-type "text/xml")))
