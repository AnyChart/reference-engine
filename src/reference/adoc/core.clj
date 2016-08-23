(ns reference.adoc.core
  (:require [clojure.java.io :refer [file]]
            [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.saver :refer [save-entries]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.adoc.media :refer [move-media]]
            [reference.adoc.typescript :as ts]
            [reference.adoc.categories :refer [parse-categories-order build-class-categories build-namespace-categories]]
            [reference.git :as git]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [reference.data.search :as search-data]
            [reference.data.sitemap :as sitemap]
            [reference.components.notifier :as notifications]
            [me.raynes.fs :as fs]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre :refer [info error]]))

(defn- update-branches [show-branches git-ssh data-dir]
  (let [repo-path (str data-dir "/repo/")
        versions-path (str data-dir "/versions/")]
    (git/update git-ssh repo-path)
    (let [branches (if show-branches
                     (git/actual-branches git-ssh repo-path)
                     (git/version-branches git-ssh repo-path))]
      (doall (pmap #(git/checkout git-ssh repo-path % (str versions-path %))
                   branches))
      (git/get-hashes git-ssh versions-path branches))))

(defn- remove-branch [jdbc branch-key]
  (let [version-id (:id (vdata/version-by-key jdbc branch-key))]
    (pdata/delete-version-pages jdbc version-id)
    (sitemap/remove-by-version jdbc version-id)
    (vdata/delete-by-id jdbc version-id)))

(defn- remove-branches [jdbc actual-branches data-dir]
  (info "actual branches" (vec actual-branches))
  (let [current-branches (vdata/versions jdbc)
        removed-branches (filter #(not (some #{%} actual-branches)) current-branches)]
    (info "current branches" (vec current-branches))
    (info "removed branches" (vec removed-branches))
    (if (seq removed-branches)
      (doseq [branch-key removed-branches]
        (remove-branch jdbc branch-key)
        (git/run-sh "rm" "-rf" (str data-dir "/versions-static/" branch-key))))
    removed-branches))

(defn- filter-for-rebuild [jdbc branches]
  (filter #(vdata/need-rebuild? jdbc (:name %) (:commit %)) branches))

(defn- build-media [jdbc version-id version-key data-dir]
  (move-media version-key (str data-dir "/versions/") (str data-dir "/versions-static/")))

(defn- build-pages [jdbc version-id version-key top-level docs playground]
  (save-entries jdbc version-id version-key top-level))

(defn- remove-previous-versions [jdbc actual-id key]
  (let [ids (vdata/version-ids jdbc key)
        outdated-ids (filter #(not= actual-id %) ids)]
    (doall (map (fn [vid]
                  (pdata/delete-version-pages jdbc vid)
                  (sitemap/remove-by-version jdbc vid)
                  (vdata/delete-by-id jdbc vid))
                outdated-ids))))

(defn- get-version-config [data-dir version-key]
  (if (.exists (file (str data-dir "/versions/" version-key "/.api-config.edn")))
    (read-string (slurp (str data-dir "/versions/" version-key "/.api-config.edn")))
    {:samples true}))

(defn build-branch
  [branch jdbc notifier git-ssh data-dir max-processes jsdoc-bin docs playground queue-index]
  (try
    (do
      (info "building" branch)
      (notifications/start-version-building notifier (:name branch) queue-index)
      (let [categories-order (parse-categories-order data-dir (:name branch))
            doclets (get-doclets data-dir max-processes jsdoc-bin (:name branch))
            raw-top-level (structurize doclets data-dir (:name branch))
            inh-top-level (assoc raw-top-level
                                 :classes (build-inheritance (:classes raw-top-level)))
            top-level (assoc inh-top-level
                             :namespaces (doall (map #(build-namespace-categories
                                                       % categories-order)
                                                     (:namespaces inh-top-level)))
                             :classes (doall (map #(build-class-categories
                                                    % categories-order)
                                                  (:classes inh-top-level))))
            tree-data (generate-tree top-level)
            search-index (generate-search-index top-level)
            config (get-version-config data-dir (:name branch))]
        (info "categories order:" categories-order)
        (let [version-id (vdata/add-version jdbc
                                            (:name branch)
                                            (:commit branch)
                                            tree-data search-index
                                            (:samples config))]
          (build-pages jdbc version-id (:name branch) top-level docs playground)
          (build-media jdbc version-id (:name branch) data-dir)
          (sitemap/update-sitemap jdbc version-id top-level)
          (ts/generate-ts-declarations data-dir (:name branch) top-level)
          (remove-previous-versions jdbc version-id (:name branch))))
      (notifications/complete-version-building notifier (:name branch) queue-index)
      true)
    (catch Exception e
      (do (error e)
          (error (.getMessage e))
          (notifications/build-failed notifier (:name branch) queue-index)
          nil))))

;(defn- build-experiments [dev]
;  (build-branch {:name "experiments" :commit (System/currentTimeMillis)}
;                (-> dev :generator :jdbc)
;                (-> dev :generator :notifier)
;                (-> dev :generator :config :git-ssh)
;                (-> dev :generator :config :data-dir)
;                (-> dev :generator :config :max-processes)
;                (-> dev :generator :config :jsdoc-bin) "" ""))

(defn build-all
  [jdbc notifier
   {:keys [show-branches git-ssh data-dir max-processes jsdoc-bin docs playground]} queue-index]
  (fs/mkdirs (str data-dir "/versions/"))
  (fs/mkdirs (str data-dir "/versions-tmp/"))
  (let [actual-branches (update-branches show-branches git-ssh data-dir)
        removed-branches (remove-branches jdbc (map :name actual-branches) data-dir)
        branches (filter-for-rebuild jdbc actual-branches)
        branch-names (map :name branches)]
    (notifications/start-building notifier branch-names removed-branches queue-index)
    (let [result (doall (map #(build-branch %
                                            jdbc
                                            notifier
                                            git-ssh
                                            data-dir
                                            max-processes
                                            jsdoc-bin
                                            docs
                                            playground
                                            queue-index)
                                 branches))]
      ;(when (or (not-empty removed-branches)
      ;          (not-empty branches))
      ;  (notifications/start-database-refresh notifier)
      ;  (search-data/refresh jdbc))
      (fs/delete-dir (str data-dir "/versions/"))
      (fs/delete-dir (str data-dir "/versions-tmp/"))
      (if (some nil? result)
        (notifications/complete-building-with-errors notifier branch-names queue-index)
        (notifications/complete-building notifier branch-names removed-branches queue-index)))))
