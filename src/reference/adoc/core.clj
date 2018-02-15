(ns reference.adoc.core
  (:require [clojure.java.io :refer [file]]
            [reference.adoc.adoc :refer [get-doclets]]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :refer [build-inheritance]]
            [reference.adoc.saver :refer [save-entries]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.adoc.media :refer [move-media]]
            [reference.adoc.defs.ts.typescript :as ts]
            [reference.adoc.defs.ts.tree :as tree-ts]
            [reference.adoc.defs.tern :as tern]
            [reference.adoc.defs.json :as json-gen]
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


(defn actual-branches [show-branches git-ssh repo-path]
  (if show-branches
    (git/actual-branches-with-hashes git-ssh repo-path)
    (git/version-branches-with-hashes git-ssh repo-path)))


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
  [branch jdbc notifier git-ssh data-dir max-processes jsdoc-bin docs playground queue-index latest-version-key]
  (try
    (do
      (info "building" branch)
      (notifications/start-version-building notifier branch queue-index)

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
            search-index (generate-search-index top-level (str data-dir "/versions/" (:name branch) "/_search"))
            config (get-version-config data-dir (:name branch))]

        ;(when (= (:name branch) "develop")
        ;  (ts/set-top-level! top-level)
        ;  (tern/set-top-level! top-level tree-data)
        ;  (json-gen/set-top-level! top-level)
        ;  )

        (info "categories order:" categories-order)
        (let [version (vdata/add-version jdbc
                                         (:name branch)
                                         (:commit branch)
                                         tree-data
                                         search-index
                                         (:samples config))
              version-id (:id version)]
          (save-entries jdbc version (:name branch) top-level docs playground)
          (build-media jdbc version-id (:name branch) data-dir)
          (sitemap/update-sitemap jdbc version-id top-level)

          (remove-previous-versions jdbc version-id (:name branch))

          (tern/generate-declarations {:data-dir    data-dir
                                       :version-key (:name branch)
                                       :domain      (-> notifier :config :domain)} tree-data top-level)

          (json-gen/generate data-dir (:name branch) latest-version-key top-level)

          (let [ts-result (ts/generate-ts-declarations data-dir
                                                       git-ssh
                                                       (:name branch)
                                                       latest-version-key
                                                       (tree-ts/modify top-level)
                                                       notifier)]
            (if (zero? (:exit ts-result))
              (do (notifications/complete-version-building notifier (:name branch) queue-index) true)
              (notifications/build-failed notifier (:name branch) queue-index nil ts-result))))))
    (catch Exception e
      (do (error e)
          (error (.getMessage e))
          (notifications/build-failed notifier (:name branch) queue-index e nil)
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
   {:keys [show-branches git-ssh data-dir max-processes jsdoc-bin docs playground]}
   queue-index]
  (try
    (let [repo-path (str data-dir "/repo/")
          versions-path (str data-dir "/versions/")
          versions-tmp (str data-dir "/versions-tmp/")]
      (fs/mkdirs versions-path)
      (fs/mkdirs versions-tmp)
      (git/update git-ssh repo-path)
      (let [actual-branches (actual-branches show-branches git-ssh repo-path)
            removed-branches (remove-branches jdbc (map :name actual-branches) data-dir)
            branches (filter-for-rebuild jdbc actual-branches)
            branch-names (map :name branches)
            latest-version-key (vdata/default jdbc branch-names)]
        (doall (pmap #(git/checkout git-ssh repo-path % (str versions-path %)) branch-names))
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
                                                queue-index
                                                latest-version-key)
                                 branches))]
          ;(when (or (not-empty removed-branches)
          ;          (not-empty branches))
          ;  (notifications/start-database-refresh notifier)
          ;  (search-data/refresh jdbc))
          (fs/delete-dir versions-path)
          (fs/delete-dir versions-tmp)
          (if (some nil? result)
            (notifications/complete-building-with-errors notifier branch-names queue-index)
            (notifications/complete-building notifier branch-names removed-branches queue-index)))))
    (catch Exception e
      (do (timbre/error e)
          (timbre/error (.getMessage e))
          (notifications/complete-building-with-errors notifier [] queue-index e)))))
