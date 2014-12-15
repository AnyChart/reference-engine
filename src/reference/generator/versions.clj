(ns reference.generator.versions
  (:require [taoensso.carmine :as car :refer (wcar)]
            [reference.generator.git :as git]
            [reference.generator.exports :as exports]
            [reference.config :as config]
            [reference.generator.core :refer [get-namespaces get-top-level]]
            [reference.generator.tree :as tree-gen]
            [reference.generator.generator :as html-gen]
            [reference.generator.search :as search-gen]
            [reference.generator.samples :as samples]
            [reference.data.versions :as vdata]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]))

(defn force-build-version-without-git [version]
  ;;(samples/checkout-version version)
  
  (let [base-path (str config/data-path "versions/" version "/")
        acdvf-src (str base-path "src/data")
        graphics-src (str base-path "contrib/graphics/src/")
        exports-data (exports/add-exports-from-file
                      (str graphics-src "export.js")
                      (exports/add-export-from-folder acdvf-src))
        namespaces-data (get-namespaces version
                                        exports-data
                                        ;;graphics-src
                                        acdvf-src)
        tree-data (tree-gen/generate-tree namespaces-data)
        search-index (search-gen/build-index namespaces-data)]
    (doall (html-gen/pre-render-top-level version (get-top-level namespaces-data)))
    (vdata/add-version version (generate-string tree-data)
                       (generate-string search-index))
    ;;(vdata/update-hash version commit)

    ;;(samples/commit-version version)
    ;;(notify-slack version)
    (println version "- done!")))

;;(force-build-version-without-git "develop")

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
        (samples/checkout-version version)
        
        (let [base-path (str config/data-path "versions/" version "/")
              acdvf-src (str base-path "src/")
              graphics-src (str base-path "contrib/graphics/src/")
              exports-data (exports/add-exports-from-file
                            (str graphics-src "export.js")
                            (exports/add-export-from-folder acdvf-src))
              namespaces-data (get-namespaces version
                                              exports-data
                                              graphics-src
                                              acdvf-src)
              tree-data (tree-gen/generate-tree namespaces-data)
              search-index (search-gen/build-index namespaces-data)]
          (doall (html-gen/pre-render-top-level version (get-top-level namespaces-data)))
          (vdata/add-version version (generate-string tree-data)
                             (generate-string search-index))
          (vdata/update-hash version commit)

          (samples/commit-version version)
          (notify-slack version)
          (println version "- done!"))))))

(defn build []
  (samples/update)
  (let [branches (git/update (fn [branch-name] true))]
    (println "branches" (map :name branches))
    (doall (map build-branch branches))))

;;(time (build))
