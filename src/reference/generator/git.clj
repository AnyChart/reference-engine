(ns reference.generator.git
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]
            [reference.config :as config]
            [clojure.java.shell :refer [sh]]
            [clojure.java.io :refer [file]]))

(defmacro auth-git [ & body ]
  `(binding [git/*ssh-identity-name* "github.com"
             git/*ssh-exclusive-identity* true
             git/*ssh-prvkey* (slurp (str config/keys-path "id_rsa"))
             git/*ssh-pubkey* (slurp (str config/keys-path "id_rsa.pub"))]
     ~@body))

(defn- get-repo []
  (git/load-repo (str config/data-path "/repo")))

(defn- branches [repo]
  (gitq/branch-list-with-heads repo))

(defn- get-branch-name [branch]
  (last (re-find #"refs/heads/(.*)$" (.getName branch))))

(defn- checkout-branch [[branch commit] repo]
  (if-let [short-name (get-branch-name branch)]
    (do
      (println "checkouting" short-name)
      (sh "copy" "-r"
          (str config/data-path "/repo")
          (str config/data-path "/versions/" short-name))
      (git/git-checkout (git/load-repo (str config/data-path "/versions/" short-name))
                        short-name))))

(defn update-submodules [short-name]
  (let [repo (git/load-repo (str config/data-path "/versions/" short-name))]
        (git/git-submodule-update repo)))

(defn- delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (file fname))))

(defn update [branches-filter]
  (let [repo (get-repo)]
    (auth-git
     (git/git-fetch-all repo)
     (git/git-submodule-sync repo)
     (println "===")
     (println (git/git-submodule-update repo))
     (let [versions-base-path (str config/data-path "/versions")
           branches (filter (fn [[branch commit]]
                              (branches-filter (get-branch-name branch)))
                            (branches repo))]
       (if (.exists (file versions-base-path))
         (delete-recursively versions-base-path))
       (doall (map #(checkout-branch % repo) branches))
       (map (fn [[branch commit]]
              {:name (get-branch-name branch)
               :commit (.getName commit)}) branches)))))
