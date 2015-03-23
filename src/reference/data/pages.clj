(ns reference.data.pages
  (:require [reference.config :as config]
            [clojure.java.io :refer [as-file]]
            [taoensso.carmine :as car :refer (wcar)]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))

(defn- get-path [version page]
  (str config/data-path "versions-data/" version "/" page ".html"))

(defn page-exists? [version page]
  (let [path (get-path version page)]
    (.exists (as-file path))))

(defn get-page [version page]
  (slurp (get-path version page)))

(defn- redis-version-page-info-key [version page]
  (str "ref-version-" version "-" page "-info"))

(defn- save-page-info [version page info]
  (wcar* (car/set (redis-version-page-info-key version page)
                  info)))

(defn page-info [version page]
  (wcar* (car/get (redis-version-page-info-key version page))))

(defn add-top-level [version entries]
  (doall (map #(save-page-info version
                               (:full-name %)
                               {:kind :namespace}) (:namespaces entries)))
  (doall (map #(save-page-info version
                               (:full-name %)
                               {:kind :class}) (:classes entries)))
  (doall (map #(save-page-info version
                               (:full-name %)
                               {:kind :typedef}) (:typedefs entries)))
  (doall (map #(save-page-info version
                               (:full-name %)
                               {:kind :enum}) (:enums entries))))
