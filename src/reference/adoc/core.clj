(ns reference.adoc.core
  (:require [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.htmlgen :refer [pre-render-top-level]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.git :as git]
            [reference.data.versions :as vdata]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre :refer [info]]))

(defn build [version]
  (info "build" version)
  (let [doclets (get-doclets version)
        raw-top-level (structurize doclets)
        top-level (assoc raw-top-level
                         :classes (build-inheritance (:classes raw-top-level)))
        tree-data (generate-tree top-level)
        search-index (generate-search-index top-level)]
    (doall (pre-render-top-level version top-level))
    (vdata/add-version version
                       (generate-string tree-data)
                       (generate-string search-index))
    (info "building" version "completed")))

(defn notify-slack [version]
  (if (not (empty? version))
    (http/post "https://anychart-team.slack.com/services/hooks/incoming-webhook?token=P8Z59E0kpaOqTcOxner4P5jb"
               {:form-params {:payload (generate-string {:text
                                                         (str "API reference generated for " version)
                                                         :channel "#notifications"
                                                         :username "api-reference"})}})))

(defn- build-branch [branch]
  (let [version (:name branch)
        commit (:commit branch)
        saved-commit (vdata/get-hash version)]
    (if-not (= commit saved-commit)
      (do
        (info "building" version)
        (let [doclets (get-doclets version)
              raw-top-level (structurize doclets)
              top-level (assoc raw-top-level
                               :classes (build-inheritance (:classes raw-top-level)))
              tree-data (generate-tree top-level)
              search-index (generate-search-index top-level)]
          (doall (pre-render-top-level version top-level))
          (vdata/add-version version
                             (generate-string tree-data)
                             (generate-string search-index))
          (vdata/update-hash version commit)
          (notify-slack version)
          (info "building" version "completed"))))))

(defn build-all []
  (let [branches (git/update (fn [branch-name] true))]
    (info "branches:" (map :name branches))
    (doall (map build-branch branches))))

;; (build "master")

;;(build-all)

;;(build "DVF-1245_rework_radar_polar")
