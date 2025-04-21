(ns reference.adoc.core
  (:require [reference.adoc.adoc :as adoc]
            [reference.adoc.structs :refer [structurize]]
            [reference.adoc.inheritance :as inh]
            [reference.adoc.saver :refer [save-entries]]
            [reference.adoc.tree :refer [generate-tree]]
            [reference.adoc.tree-minimized :as tree-minimized]
            [reference.adoc.search :refer [generate-search-index]]
            [reference.adoc.media :refer [move-media]]
            [reference.adoc.defs.ts.typescript :as ts]
            [reference.adoc.defs.ts.tree :as tree-ts]
            [reference.adoc.defs.tern.core :as tern]
            [reference.adoc.defs.json.core :as json-gen]
            [reference.adoc.typedef-builder :as typedef-builder]
            [reference.adoc.categories :as categories]
            [reference.git :as git]
            [reference.data.versions :as vdata]
            [reference.data.pages :as pdata]
            [reference.data.search :as search-data]
            [reference.data.sitemap :as sitemap]
            [reference.components.notifier :as notifications]
            [com.rpl.specter :refer :all]
            [clojure.java.io :refer [file]]
            [me.raynes.fs :as fs]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre :refer [info error debug]]
            [reference.util.utils :as utils]
            [clojure.string :as string]
            [cheshire.core :as json]
            [reference.adoc.defs.ts.check :as ts-check]
            [reference.config.core :as c]
            [clojure.stacktrace]))


(defn actual-branches [show-branches git-ssh repo-path]
  (if show-branches
    (git/actual-branches-with-hashes git-ssh repo-path)
    (git/version-branches-with-hashes git-ssh repo-path)))


(defn- remove-branches [jdbc actual-branches data-dir]
  (info "actual branches" (vec actual-branches))
  (let [current-branches (vdata/versions jdbc)
        removed-branches (filter #(not (some #{%} actual-branches)) current-branches)]
    (info "current branches" (vec current-branches))
    (info "removed branches" (vec removed-branches))
    (if (seq removed-branches)
      (doseq [branch-key removed-branches]
        (vdata/remove-branch-by-key jdbc branch-key)
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


(defn build-typescript [data-dir
                        git-ssh
                        branch
                        latest-version-key
                        notifier
                        all-doclets
                        categories-order
                        tree-data]
  (let [raw-top-level (structurize all-doclets data-dir (:name branch))
        inh-top-level (inh/build-inheritance raw-top-level)
        top-level (categories/categorize inh-top-level categories-order)

        top-level-ts (typedef-builder/fix-typedef top-level :typescript true)
        replaced-top-level-ts (tree-ts/update-classes-methods top-level-ts :add-parent-methods true)

        top-level-js (typedef-builder/fix-typedef top-level :typescript false)
        replaced-top-level-js (tree-ts/update-classes-methods top-level-js :add-parent-methods true)]
    (json-gen/generate data-dir (:name branch) latest-version-key replaced-top-level-js)

    (tern/generate-declarations {:data-dir    data-dir
                                 :version-key (:name branch)
                                 :domain      "https://api.anychart.com/"
                                 ;:domain     (c/domain)
                                 }
                                tree-data
                                replaced-top-level-js)

    ;; generate graphics.d.ts
    (let [graphics-top-level (-> replaced-top-level-ts
                                 (update :namespaces (fn [ns] (filter #(string/starts-with? (:full-name %) "anychart.graphics") ns)))
                                 (update :classes (fn [ns] (filter #(string/starts-with? (:full-name %) "anychart.graphics") ns)))
                                 (update :typedefs (fn [ns] (filter #(string/starts-with? (:full-name %) "anychart.graphics") ns)))
                                 (update :enums (fn [ns] (filter #(string/starts-with? (:full-name %) "anychart.graphics") ns))))

          ;; delete 'credits' method from anychart.graphics.vector.Stage - cause it returns
          ;; non-graphics class: anychart.core.ui.StageCredits
          ;; http://api.anychart.stg/anychart.graphics.vector.Stage#credits
          graphics-top-level (setval [:classes
                                      ALL
                                      #(= "anychart.graphics.vector.Stage" (:full-name %))
                                      :methods
                                      ALL
                                      #(= "credits" (:name %))]
                                     NONE
                                     graphics-top-level)

          _ (ts-check/check-prepare data-dir (:name branch) git-ssh)

          graphics-ts-data (ts/generate-graphics-ts-declarations data-dir
                                                                 git-ssh
                                                                 (:name branch)
                                                                 latest-version-key
                                                                 graphics-top-level
                                                                 notifier)
          graphics-ts-result (ts-check/check-graphics graphics-ts-data data-dir (:name branch))

          ; Anton Kagakin:
          ; а можно при генерации anychart.d.ts не упоминать anychart.graphics.math.rect - выпилить из генерации?
          ; просто получится
          ; anychart.math.rect: anychart.math.Rect - алиас на anychart.graphics.math.Rect
          ; anychart.graphics.math.rect: anychart.graphics.math.Rect
          anychart-top-level (setval [:namespaces
                                      ALL
                                      #(= "anychart.graphics.math" (:full-name %))
                                      :functions
                                      ALL
                                      #(= "rect" (:name %))]
                                     NONE
                                     replaced-top-level-ts)

          ;; generate index.d.ts
          index-ts-data (ts/generate-ts-declarations data-dir
                                                     git-ssh
                                                     (:name branch)
                                                     latest-version-key
                                                     anychart-top-level
                                                     notifier)
          index-ts-result (ts-check/check-index index-ts-data data-dir (:name branch))

          _ (ts-check/check-clean data-dir (:name branch))]
      {:index-ts-result    (merge index-ts-data index-ts-result)
       :graphics-ts-result (merge graphics-ts-data graphics-ts-result)})))


(defn need-generate-ts [branch gen-params]
  (cond
    (and (= (:name branch) (:version gen-params)) (:fast gen-params)) false
    (and (= (:name branch) (:version gen-params)) (:dts gen-params)) true
    (= (c/prefix) "local") false
    :else (or (utils/released-version? (:name branch))
              ;(= (:name branch) "develop")
              (= (:name branch) "master")
              (string/includes? (:message branch) "#dts")
              (string/includes? (:message branch) "#ts")
              (string/includes? (:message branch) "#all"))))


(defn build-branch
  [branch jdbc notifier git-ssh data-dir max-processes jsdoc-bin docs playground queue-index
   latest-version-key gen-params]
  (try
    (do
      (info "building" branch)
      (debug "DEBUG: build-branch with params:" 
             "\nbranch:" branch 
             "\ngit-ssh:" git-ssh
             "\ndata-dir:" data-dir
             "\nmax-processes:" max-processes
             "\njsdoc-bin:" jsdoc-bin)
      (debug "DEBUG: Checking if directories exist:"
             "\ndata-dir exists?" (.exists (file data-dir))
             "\ndata-dir/repo exists?" (.exists (file (str data-dir "/repo/")))
             "\ndata-dir/versions exists?" (.exists (file (str data-dir "/versions/"))))
      
      (notifications/start-version-building notifier branch queue-index)

      (let [categories-order (categories/parse-categories-order data-dir (:name branch))
            _ (debug "DEBUG: categories-order:" categories-order)
            
            _ (debug "DEBUG: About to call adoc/get-all-doclets")
            all-doclets (adoc/get-all-doclets data-dir max-processes jsdoc-bin (:name branch))
            _ (debug "DEBUG: all-doclets count:" (count all-doclets))
            
            doclets (adoc/get-not-ignored-doclets all-doclets)
            _ (debug "DEBUG: filtered doclets count:" (count doclets))
            
            _ (debug "DEBUG: About to structurize doclets")
            raw-top-level (structurize doclets data-dir (:name branch))
            _ (debug "DEBUG: raw-top-level:" (str (keys raw-top-level)))
            
            _ (debug "DEBUG: About to build inheritance")
            inh-top-level (inh/build-inheritance raw-top-level)
            _ (debug "DEBUG: inh-top-level:" (str (keys inh-top-level)))
            
            top-level (categories/categorize inh-top-level categories-order)
            top-level (typedef-builder/fix-typedef top-level :typescript false)
            top-level (tree-ts/update-classes-methods top-level :add-parent-methods false)
            
            _ (debug "DEBUG: About to generate tree")
            tree-data (generate-tree top-level)
            _ (debug "DEBUG: tree-data generated, count:" (count tree-data) "empty?" (empty? tree-data))
            _ (when (empty? tree-data) (debug "WARNING: tree-data is empty! This may cause UI navigation problems."))
            
            tree-min-data (tree-minimized/generate-tree top-level)
            _ (debug "DEBUG: tree-min-data generated, count:" (count tree-min-data) "empty?" (empty? tree-min-data))
            _ (when (empty? tree-min-data) 
                (error "ERROR: tree-min-data is empty! This will result in empty tree in database.")
                (debug "DEBUG: Top-level structure:" (str (keys top-level)))
                (debug "DEBUG: Namespace count:" (count (:namespaces top-level))))
            
            search-index (generate-search-index top-level (str data-dir "/versions/" (:name branch) "/_search"))
            _ (debug "DEBUG: search-index generated")
            
            config (get-version-config data-dir (:name branch))
            _ (debug "DEBUG: config:" config)]

        (when (= (:name branch) "v8")
          (ts/set-top-level! top-level)
          (tern/set-top-level! top-level tree-data)
          (json-gen/set-top-level! top-level)
          (typedef-builder/set-top-level! top-level))

        (info "categories order:" categories-order)
        (debug "DEBUG: About to add version to database")
        (let [version (vdata/add-version jdbc
                                         (:name branch)
                                         (:commit branch)
                                         tree-min-data
                                         search-index
                                         (:samples config))
              version-id (:id version)]
          (debug "DEBUG: Version added to database:" version)
          
          (debug "DEBUG: About to save entries")
          (save-entries jdbc version (:name branch) top-level docs playground)
          (debug "DEBUG: Entries saved")
          
          (debug "DEBUG: About to build media")
          (build-media jdbc version-id (:name branch) data-dir)
          (debug "DEBUG: Media built")
          
          (debug "DEBUG: About to update sitemap")
          (sitemap/update-sitemap jdbc version-id top-level)
          (debug "DEBUG: Sitemap updated")

          (debug "DEBUG: About to remove previous versions")
          (remove-previous-versions jdbc version-id (:name branch))
          (debug "DEBUG: Previous versions removed")

          (if (need-generate-ts branch gen-params)
            (let [ts-result (build-typescript data-dir git-ssh branch latest-version-key notifier all-doclets categories-order tree-data)]
              (if (and (zero? (-> ts-result :index-ts-result :exit))
                       (zero? (-> ts-result :graphics-ts-result :exit)))
                (do (notifications/complete-version-building notifier branch queue-index true) true)
                (notifications/complete-version-building-error notifier branch queue-index nil ts-result)))
            (do (notifications/complete-version-building notifier branch queue-index false) true)))))
    (catch Exception e
      (do (error e)
          (error (.getMessage e))
          (debug "DEBUG: Exception stacktrace:" (with-out-str (clojure.stacktrace/print-stack-trace e)))
          (notifications/complete-version-building-error notifier (:name branch) queue-index e nil)
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
   queue-index
   gen-params]
  (try
    (debug "DEBUG: Starting build-all with parameters:"
           "\nshow-branches:" show-branches
           "\ngit-ssh:" git-ssh
           "\ndata-dir:" data-dir
           "\nmax-processes:" max-processes
           "\njsdoc-bin:" jsdoc-bin
           "\ndocs:" docs
           "\nplayground:" playground
           "\nqueue-index:" queue-index
           "\ngen-params:" gen-params)
    
    (let [repo-path (str data-dir "/repo/")
          versions-path (str data-dir "/versions/")
          versions-tmp (str data-dir "/versions-tmp/")]
      (debug "DEBUG: Setting paths:"
             "\nrepo-path:" repo-path
             "\nversions-path:" versions-path
             "\nversions-tmp:" versions-tmp)
      
      (debug "DEBUG: Checking if directories exist:"
             "\ndata-dir exists?" (.exists (file data-dir))
             "\nrepo-path exists?" (.exists (file repo-path)))
      
      (fs/mkdirs versions-path)
      (fs/mkdirs versions-tmp)
      (debug "DEBUG: Created versions directories")
      
      (debug "DEBUG: About to update git repository")
      (git/update git-ssh repo-path)
      (let [actual-branches (actual-branches show-branches git-ssh repo-path)
            _ (debug "DEBUG: Got actual branches:" (map :name actual-branches))
            
            removed-branches (remove-branches jdbc (map :name actual-branches) data-dir)
            _ (debug "DEBUG: Removed branches:" removed-branches)
            
            branches (filter-for-rebuild jdbc actual-branches)
            _ (debug "DEBUG: Branches to rebuild:" (map :name branches))
            
            branch-names (map :name branches)
            latest-version-key (vdata/default jdbc branch-names)
            _ (debug "DEBUG: Latest version key:" latest-version-key)]
        
        (doall (pmap #(git/checkout git-ssh repo-path % (str versions-path %)) branch-names))
        (debug "DEBUG: Checked out all branches")
        
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
                                                latest-version-key
                                                gen-params)
                                 branches))]
          (debug "DEBUG: Build results:" (map #(if % "success" "failure") result))
          
          (fs/delete-dir versions-path)
          (fs/delete-dir versions-tmp)
          (debug "DEBUG: Cleaned up temporary directories")
          
          (if (some nil? result)
            (do
              (debug "DEBUG: Some builds failed")
              (notifications/complete-building-with-errors notifier branch-names queue-index))
            (do
              (debug "DEBUG: All builds successful")
              (notifications/complete-building notifier branch-names removed-branches queue-index))))))
    (catch Exception e
      (do (timbre/error e)
          (timbre/error (.getMessage e))
          (debug "DEBUG: Exception stacktrace:" (with-out-str (clojure.stacktrace/print-stack-trace e)))
          (notifications/complete-building-with-errors notifier [] queue-index e)))))
