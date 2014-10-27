(ns reference-engine.projects
  (:require [reference-engine.config :as config]
            [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]
            [cheshire.core :refer [generate-string]]
            [taoensso.carmine :as car]
            [reference-engine.db :refer (wcar*)]
            [reference-engine.git :as git]
            [reference-engine.generator :as docs-generator]
            [reference-engine.samples :as samples]))

(defn redis-key-projects []
  (str "ref_projects"))

(defn redis-key-project-versions [project]
  (str "ref_project_" project "_version"))

(defn redis-key-project-version-hash [project version]
  (str "ref_project_" project "_hash_version_" version))

(defn redis-key-entry [project version name]
  (str "ref_project_" project "_version_" version "_" name))

(defn redis-key-entry-mask [project version]
  (str "ref_project_" project "_version_" version "_*"))

(defn redis-key-namespaces [project version]
  (str "ref_project_" project "_version_" version "_namespaces"))

(defn redis-key-tree [project version]
  (str "ref_project_" project "_version_" version "_tree"))

;; projects

(defn get-all []
  (wcar* (car/smembers (redis-key-projects))))

(defn get-projects-from-fs []
  (map #(.getName %) (filter #(and (.isDirectory %)
                              (not (.isHidden %)))
                        (.listFiles (file config/data-path)))))

(defn exists? [project]
  (= 1 (wcar* (car/sismember (redis-key-projects) project))))

(defn default-project [] "acdvf")

;; versions

(defn versions [project]
  (wcar* (car/smembers (redis-key-project-versions project))))

(defn version-exists? [project version]
  (= 1 (wcar* (car/sismember (redis-key-project-versions project) version))))

(defn version-default [project]
  (first (versions project)))

(defn get-versions-from-repo [project]
  (let [path (str config/data-path project "/repo")]
    (log/info path)
    (if config/show-branches
      (git/actual-branches path)
      (git/version-branches path))))

;; namespaces

(defn namespaces [project version]
  (sort (wcar* (car/smembers (redis-key-namespaces project version)))))

(defn namespace-default [project version]
  (first (namespaces project version)))

;; entries

(defn tree [project version]
  (wcar* (car/get (redis-key-tree project version))))

(defn has-entry [project version name]
  (= 1 (wcar* (car/exists (redis-key-entry project version name)))))

(defn get-entry [project version name]
  (wcar* (car/get (redis-key-entry project version name))))

;; updating

(defn do-generate-docs [project version path]
  (docs-generator/generate-for-server
   project version
   (str path)
   (fn [parse-result]
     (let [namespaces (:namespaces parse-result)]
       (log/info "namespaces: " (map :full-name namespaces))
       (wcar* (car/set (redis-key-tree project version)
                       (generate-string (:tree parse-result))))
       (wcar* (apply car/sadd (redis-key-namespaces project version)
                     (map :full-name namespaces)))))
   (fn [entry]
     (wcar* (car/set (redis-key-entry project version (:full-name entry))
                     entry))
     entry)
   (fn [obj-name sample]
     (samples/parse-sample-server (str config/data-path project)
                                  version
                                  obj-name
                                  sample))))

(defn generate-version-docs [project version]
  (log/info "generating docs for" project "version" version)
  (let [version-path (str config/data-path
                          project
                          "/versions/"
                          version)]
    (git/checkout-to (str config/data-path
                          project
                          "/repo/")
                     version
                     version-path)
    (let [hash (git/get-hash version-path)
          saved-hash (wcar* (car/get (redis-key-project-version-hash project version)))]
      (if (not (= hash saved-hash))
        (do
          (git/update-submodules version-path)
          (do-generate-docs project version version-path)
          (wcar* (car/set (redis-key-project-version-hash project version) hash))
          version)
        (do (log/info version "already built, ignoring")
            nil)))))

(defn cleanup-version [project version]
  (wcar* (car/del (redis-key-namespaces project version)))
  (map #(wcar* (car/del %))
       (wcar* (car/keys (redis-key-entry-mask project version)))))

(defn cleanup-project [project]
  (git/run-sh "rm" "-rf" (str config/data-path
                              project
                              "/versions/*"))
  (git/run-sh "rm" "-rf" (str config/data-path
                              project
                              "/samples-versions/*"))
  (git/run-sh "rm" "-rf" (str config/data-path
                              project
                              "/samples-data/*"))
  (map #(cleanup-version project %) (versions project))
  (wcar* (car/del (redis-key-project-versions project))))

(defn update-project [project]
  (log/info "updating" project)
  (cleanup-project project)
  (samples/update-repo (str config/data-path project))
  (git/update (str config/data-path project "/repo/"))
  (let [versions (get-versions-from-repo project)]
    (log/info "versions:" versions)
    (wcar* (apply car/sadd (redis-key-project-versions project) versions))
    (let [updated-versions (doall (pmap #(generate-version-docs project %) versions))]
      (samples/synchronize-repo (str config/data-path project) versions)
      updated-versions)))

(defn update []
  (let [p (get-projects-from-fs)]
    (wcar* (car/del (redis-key-projects)))
    (wcar* (apply car/sadd (redis-key-projects) p))
    (log/info "updating projects:")
    (doall (map update-project p))
    (log/info "all projects updated")))

;;(update)
