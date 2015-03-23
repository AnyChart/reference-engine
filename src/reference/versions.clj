(ns reference.versions
  (:require [clojure.java.io :refer [file]]
            [taoensso.timbre :as timbre :refer [info]]
            [reference.config :as config]
            [reference.data.versions :as vdata]
            [reference.git :as git]))

(defn- local-branches []
  (vdata/all-versions))

(defn- outdated-branches [actual-branches current-branches]
  (clojure.set/difference (set actual-branches) (set current-branches)))

(defn- remove-branch [version]
  (info "removing" version)
  (vdata/remove-version version)
  (git/run-sh "rm" "-rf" (str config/data-path "/versions/" version))
  (git/run-sh "rm" "-rf" (str config/data-path "/versions-data/" version)))

(defn remove-unused-branches [actual-branches]
  (let [diff (outdated-branches actual-branches (local-branches))]
    (doall (map remove-branch diff))
    diff))
