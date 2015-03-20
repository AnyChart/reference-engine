(ns reference.git
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]
            [reference.config :as config]
            [clojure.java.shell :refer [sh with-sh-env with-sh-dir]]
            [clojure.java.io :refer [file]]
            [taoensso.timbre :as timbre :refer [info]]))

(def env-config {:GIT_SSH (str config/keys-path "git")})

(defmacro auth-git [ & body ]
  `(binding [git/*ssh-identity-name* "github.com"
             git/*ssh-exclusive-identity* true
             git/*ssh-prvkey* (slurp (str config/keys-path "id_rsa"))
             git/*ssh-pubkey* (slurp (str config/keys-path "id_rsa.pub"))]
     ~@body))

(defn run-sh [& command]
  (with-sh-env env-config
    (apply sh command)))

(defn run-git [path & command]
  (with-sh-env env-config
    (with-sh-dir path
      (info "git" path command)
      (let [res (apply sh "/usr/bin/git" command)]
        (info "output:" res)
        (:out res)))))

(defn- get-repo []
  (git/load-repo (str config/data-path "/repo")))

(defn- actual-branches [path]
  (map (fn [s] (last (re-matches #".*origin/([^ ]+).*" s)))
       (filter (fn [s] (and (not (= s nil))
                            (not (.contains s "->"))))
               (clojure.string/split (run-git path "branch" "-r") #"\n"))))

(defn- get-hash [path]
  (run-git path "rev-parse" "HEAD"))

(defn- branches [repo]
  (gitq/branch-list-with-heads repo))

(defn get-branch-name [branch]
  (last (re-find #"refs/heads/(.*)$" (.getName branch))))

(defn- checkout-branch [short-name repo]
  (println "checkouting" short-name)
  (sh "cp" "-r"
      (str config/data-path "/repo")
      (str config/data-path "/versions/" short-name))
  (run-git (str config/data-path "/versions/" short-name)
           "checkout" short-name))

(defn- delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (file fname))))

(defn update [branches-filter]
  (with-sh-env env-config
    (with-sh-dir (str config/data-path "/repo")
      (sh "git" "fetch")
      (sh "git" "pull")
      (sh "git" "remote" "prune" "origin")))
  
  (let [repo (get-repo)]
    (auth-git
     (let [versions-base-path (str config/data-path "/versions")
           branches (filter branches-filter
                            (actual-branches (str config/data-path "/repo")))]
       (if (.exists (file versions-base-path))
         (delete-recursively versions-base-path))
       (sh "mkdir" versions-base-path)
       (doall (map #(checkout-branch % repo) branches))
       (map (fn [name]
              {:name name
               :commit (get-hash (str config/data-path "/versions/" name))})
            branches)))))


(defn commit-and-push [path version]
  (with-sh-env env-config
    (with-sh-dir path
      (sh "git" "add" "-A" ".")
      (sh "git" "commit" "-m" "samples update")
      (sh "git" "push" "origin" version))))

(defn update-samples [path]
  (info "update" path)
  (with-sh-env env-config
    (with-sh-dir path
      (sh "git" "fetch")
      (sh "git" "pull")
      (sh "git" "remote" "prune" "origin"))))

(defn checkout-or-create [path version]
  (let [branches (actual-branches path)]
    (if (some #(= version %) branches)
      (run-git path "checkout" version)
      (run-git path "checkout" "-b" version))))
