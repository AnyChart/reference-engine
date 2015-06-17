(ns reference.adoc.core
  (:require [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.htmlgen :refer [pre-render-top-level]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.adoc.media :refer [move-media]]
            [reference.git :as git]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [reference.components.notifier :as notifications]
            [reference.config :as config]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- update-branches [show-branches git-ssh data-dir]
  (let [repo-path (str data-dir "/repo/")
        versions-path (str data-dir "/versions/")]
    (git/update git-ssh repo-path)
    (let [branches (if show-branches
                     (git/actual-branches git-ssh repo-path)
                     (git/version-branches git-ssh repo-path))]
      (doall (pmap #(git/checkout git-ssh repo-path % (str versions-path %)) branches))
      (git/get-hashes git-ssh versions-path branches))))

(defn- remove-branches [jdbc actual-branches]
  (let [current-branches (vdata/versions jdbc)
        removed-branches (filter #(some #{%} actual-branches) current-branches)]
    (if (seq removed-branches)
      (doall (map #(vdata/delete-by-key %) removed-branches)))
    removed-branches))

(defn- filter-for-rebuild [jdbc branches]
  (map :name (filter #(vdata/need-rebuild? jdbc (:name %) (:commit %)) branches)))

(defn- build-media [jdbc version-id version-key data-dir])

(defn- build-pages [jdbc version-id version-key top-level docs playground]
  (pre-render-top-level docs playground jdbc version-id version-key top-level))

(defn- build-branch
  [branch jdbc notifier git-ssh data-dir max-processes jsdoc-bin docs playground]
  (info "building" branch)
  (notifications/start-version-building notifier (:name branch))
  (let [doclets (get-doclets data-dir max-processes jsdoc-bin (:name branch))
        raw-top-level (structurize doclets (:name branch))
        top-level (assoc raw-top-level
                         :classes (build-inheritance (:classes raw-top-level)))
        tree-data (generate-tree top-level)
        search-index (generate-search-index top-level)]
    (let [version-id (vdata/add-version jdbc
                                        (:name branch)
                                        (:commit branch)
                                        tree-data search-index)]
      (build-pages jdbc version-id (:name branch) top-level docs playground)
      (build-media jdbc version-id (:name branch) data-dir)))
  (notifications/complete-version-building notifier (:name branch)))

(defn build-all
  [jdbc notifier
   {:keys [show-branches git-ssh data-dir max-processes jsdoc-bin docs playground]}]
  (notifications/start-building notifier)
  (let [actual-branches (update-branches show-branches git-ssh data-dir)
        removed-branches (remove-branches jdbc (map :name actual-branches))
        branches (filter-for-rebuild jdbc actual-branches)]
    (notifications/versions-for-build notifier branches)
    (if (seq removed-branches)
      (notifications/delete-branches notifier removed-branches))
    (doall (map #(build-branch %
                               jdbc
                               notifier
                               git-ssh
                               data-dir
                               max-processes
                               jsdoc-bin
                               docs
                               playground)
                branches))
    (notifications/complete-building notifier)))
