(ns reference.git
  (:require [clojure.java.shell :refer [sh with-sh-env with-sh-dir]]
            [taoensso.timbre :as timbre :refer [info error]]
            [clojure.string :as string]))

(defn run-sh [& command]
  (apply sh command))

(defn file-last-commit-date [base-path path]
  (info "last commit date for " (str base-path path))
  (with-sh-dir base-path
               (let [res (sh "git" "--no-pager" "log" "-1" "--format=%ct" "--" (str base-path path))]
                 (-> res
                     :out
                     (string/trim)
                     read-string))))

(defn- run-git [git-ssh path & command]
  (with-sh-env {:GIT_SSH git-ssh}
               (with-sh-dir path
                            (let [res (apply sh "/usr/bin/git" command)]
                              (println command res)
                              (:out res)))))

(defn update [git-ssh repo]
  (run-git git-ssh repo "fetch" "-p"))

(defn pull [git-ssh repo]
  (run-git git-ssh repo "pull"))

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

(defn remote-branches [git-ssh path pred]
  (let [raw-lines (string/split (run-git git-ssh path "branch" "-r" "--format='%(refname:short)|-|%(objectname)|-|%(authorname)|-|%(contents:subject)'") #"\n")
        lines (map #(string/replace % #"'" "") raw-lines)
        filtered-lines (filter (fn [s] (and (some? s)
                                            (not (.contains s "origin/HEAD"))
                                            (pred s))) lines)
        branches (map (fn [s]
                        (let [[raw-name commit author message] (string/split s #"\|-\|")]
                          {:name    (last (re-matches #"origin/(.+)" raw-name))
                           :commit  commit
                           :author  author
                           :message message})) filtered-lines)]
    branches))

(defn actual-branches-with-hashes [git-ssh path]
  (remote-branches git-ssh path (constantly true)))

(defn version-branches-with-hashes [git-ssh path]
  (remote-branches git-ssh path (fn [s] (re-find #"origin/\d+\.\d+\.\d+" s))))