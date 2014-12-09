(ns reference.generator.versions
  (:require [taoensso.carmine :as car :refer (wcar)]
            [reference.generator.git :as git]
            [reference.generator.exports :as exports]
            [reference.config :as config]
            [reference.generator.core :refer [get-namespaces get-top-level]]
            [reference.generator.tree :as tree-gen]
            [reference.generator.generator :as html-gen]
            [reference.generator.search :as search-gen]
            [reference.data.versions :as vdata]
            [cheshire.core :refer [generate-string]]))

(defn force-build-version-without-git [version]
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
    (vdata/add-version version (generate-string tree-data) (generate-string search-index))))

(defn force-build-version [version]
  (if-let [branch (git/update #{version})]
    (let [base-path (str config/data-path "versions/" version "/")
          acdvf-src (str base-path "src/")
          graphics-src (str base-path "contrib/graphics/src/")
          exports-data (exports/add-exports-from-file
                        (str graphics-src "export.js")
                        (exports/add-export-from-folder acdvf-src))
          namespaces-data (get-namespaces exports-data
                                          graphics-src
                                          acdvf-src)
          tree-data (tree-gen/generate-tree namespaces-data)
          search-index (search-gen/build-index namespaces-data)]
      (doall (html-gen/pre-render-top-level version (get-top-level namespaces-data)))
      (vdata/add-version version (generate-string tree-data) (generate-string search-index))
      (vdata/update-hash version (:commit branch))
      (println version "- done!"))
    (println "branch " version " not found")))

(defn build [])

(time
 (force-build-version-without-git "develop"))
