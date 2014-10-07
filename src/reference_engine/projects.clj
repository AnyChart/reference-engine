(ns reference-engine.projects
  (:require [reference-engine.config :as config]
            [clojure.java.io :refer [file]]
            [clojure.tools.logging :as log]
            [taoensso.carmine :as car]
            [reference-engine.db :refer (wcar*)]
            [reference-engine.git :as git]))

;; redis keys

(defn redis-key-projects []
  (str "ref_projects"))

(defn redis-key-project-versions [project]
  (str "ref_project_" project "_version"))

(defn redis-key-entry [project version name]
  (str "ref_project_" project "_version_" version "_" name))

(defn redis-key-entry-mask [project version]
  (str "ref_project_" project "_version_" version "_*"))

(defn redis-key-namespaces [project version]
  (str "ref_project_" project "_version_" version "_namespaces"))

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
  (wcar* (car/smembers (redis-key-namespaces project version))))

(defn namespace-default [project version]
  (first (namespaces project version)))

;; entries

(defn has-entry [project version name]
  (= 1 (wcar* (car/exists (redis-key-entry project version name)))))

;; updating

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
    ))

(defn cleanup-version [project version]
  (wcar* (car/del (redis-key-namespaces project version)))
  (map #(wcar* (car/del %))
       (wcar* (car/keys (redis-key-entry-mask project version)))))

(defn cleanup-project [project]
  (git/run-sh "rm" "-rf" (str config/data-path
                              project
                              "/versions/*"))
  (map #(cleanup-version project %) (versions project))
  (wcar* (car/del (redis-key-project-versions project))))

(defn update-project [project]
  (log/info "updating" project)
  (cleanup-project project)
  (git/update (str config/data-path project "/repo/"))
  (let [versions (get-versions-from-repo project)]
    (log/info "versions:" versions)
    (doall (pmap #(generate-version-docs project %) versions))))

(defn update []
  (let [p (get-projects-from-fs)]
    (wcar* (car/del (redis-key-projects)))
    (map #(wcar* (car/sadd (redis-key-projects) %)) p)
    (log/info "updating projects:")
    (doall (map update-project p))
    (log/info "all projects updated")))

(update)
