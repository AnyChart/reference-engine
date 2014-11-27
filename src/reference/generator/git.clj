(ns reference.generator.git
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]
            [reference.config :as config]
            [clojure.java.io :refer [file]]))

(defmacro auth-git [ & body ]
  `(binding [git/*ssh-identity-name* "github.com"
             git/*ssh-exclusive-identity* true
             git/*ssh-prvkey* (slurp (str config/keys-path "id_rsa"))
             git/*ssh-pubkey* (slurp (str config/keys-path "id_rsa.pub"))]
     ~@body))

(defn- get-repo [project]
  (git/load-repo (str config/data-path project "/repo")))

(defn- branches [repo]
  (gitq/branch-list-with-heads repo))

(defn- checkout-branch [[branch commit] project repo]
  (println (str config/data-path project "/versions/"))
  (if-let [short-name (last (re-find #"refs/heads/(.*)$" (.getName branch)))]
    (git/git-clone (str config/data-path project "/repo/.git")
                   (str config/data-path project "/versions/" short-name)
                   "origin"
                   short-name))
  (println "branch:" (.getName branch))
  (println "commit:" (.getName commit)))

(defn- delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (file fname))))

(defn update [project]
  (let [repo (get-repo project)]
    (auth-git
     (git/git-fetch-all repo)
     (let [versions-base-path (str config/data-path project "/versions")
           branches (branches repo)]
       (if (.exists (file versions-base-path))
         (delete-recursively versions-base-path))
       (map #(checkout-branch % project repo) branches)))))

(update "acdvf")
