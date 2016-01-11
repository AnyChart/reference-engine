(ns reference.git
  (:require [clojure.java.shell :refer [sh with-sh-env with-sh-dir]]
            [clojure.string :refer [split]]
            [taoensso.timbre :as timbre :refer [info error]]))

(defn run-sh [& command]
  (apply sh command))

(defn file-last-commit-date [base-path path]
  (info "last commit date for " (str base-path path))
  (with-sh-dir base-path
    (let [res (sh "git" "--no-pager" "log" "-1" "--format=%ct" "--" (str base-path path))]
      (-> res
          :out
          (clojure.string/trim)
          read-string))))

(defn- run-git [git-ssh path & command]
  (with-sh-env {:GIT_SSH git-ssh}
    (with-sh-dir path
      (let [res (apply sh "/usr/bin/git" command)]
        (println command res)
        (:out res)))))

(defn update [git-ssh repo]
  (run-git git-ssh repo "fetch" "-p"))

(defn update-samples [git-ssh repo branch]
  (run-git git-ssh repo "checkout" "--" "*")
  (run-git git-ssh repo "fetch" "-p")
  (run-git git-ssh repo "checkout" branch)
  (run-git git-ssh repo "pull" "origin" branch))

(defn commit-samples [git-ssh repo branch]
  (run-git git-ssh repo "pull" "origin" branch)
  (run-git git-ssh repo "add" "-A" "./")
  (run-git git-ssh repo "commit" "-m" "samples update")
  (run-git git-ssh repo "push" "origin" branch))

(defn checkout [git-ssh repo version target-path]
  (run-sh "rm" "-rf" target-path)
  (run-sh "cp" "-r" repo target-path)
  (run-git git-ssh target-path "checkout" version)
  (run-git git-ssh target-path "pull" "origin" version))


(defn- get-hash [git-ssh path]
  (clojure.string/trim-newline (run-git git-ssh path "rev-parse" "HEAD")))

(defn actual-branches [git-ssh path]
  (map (fn [s] (last (re-matches #".*origin/([^ ]+).*" s)))
       (filter (fn [s] (and (some? s)
                            (not (.contains s "->"))))
               (split (run-git git-ssh path "branch" "-r") #"\n"))))

(defn version-branches [git-ssh path]
  (map (fn [s] (re-find #"\d\.\d\.\d" s))
       (filter (fn [s] (and (not (.contains s "->"))
                            (re-matches #"[ ]+origin/\d\.\d\.\d" s)))
               (split (run-git git-ssh path "branch" "-r") #"\n"))))

(defn get-hashes [git-ssh base-path branches]
  (map (fn [branch]
         {:name branch
          :commit (get-hash git-ssh (str base-path branch))})
       branches))
