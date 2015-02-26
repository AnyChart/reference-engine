(ns reference.data.pages
  (:require [reference.config :as config]
            [clojure.java.io :refer [as-file]]))

(defn- get-path [version page]
  (str config/data-path "versions-data/" version "/" page ".html"))

(defn page-exists? [version page]
  (let [path (get-path version page)]
    (.exists (as-file path))))

(defn get-page [version page]
  (slurp (get-path version page)))
