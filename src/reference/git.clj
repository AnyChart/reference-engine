(ns reference.git
  (:require [clojure.java.shell :refer [sh with-sh-env with-sh-dir]]
            [taoensso.timbre :as timbre :refer [info error]]
            [clojure.string :as string]
            [reference.util.utils :as utils]))


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


(defn current-commit [git-ssh path]
  (subs (run-git git-ssh path "rev-parse" "HEAD") 0 7))


(defn update [git-ssh repo]
  (run-git git-ssh repo "fetch" "-p" "-P"))


(defn pull [git-ssh repo]
  (run-git git-ssh repo "pull"))


(defn clean
  "Remove all ignored, untracked files and directories"
  [git-ssh repo]
  (run-git git-ssh repo "clean" "-fxd")
  (run-git git-ssh repo "reset" "--hard"))


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


(defn remote-branches [git-ssh path]
  (let [branch-lines (string/split (run-git git-ssh path "branch" "-r" "--format='%(refname:short)|-|%(objectname)|-|%(authorname)|-|%(contents:subject)'") #"\n")
        tag-lines (string/split (run-git git-ssh path "for-each-ref" "--format='%(refname:short)|-|%(objectname)|-|%(authorname)|-|%(contents:subject)'" "refs/tags") #"\n")
        lines (map (fn [line]
                     (-> line
                         (string/replace #"^(')(.*)(')$" "$2") ;; delete start and end quotes ' ;; delete ?origin/"
                         (string/replace #"^([^/]*)/(.*)$" "$2")))
                   (sort (distinct (concat branch-lines tag-lines))))
        filtered-lines (filter (fn [s] (and (some? s)
                                            (seq s)
                                            (not (string/starts-with? s "HEAD")))) lines)
        branches (map (fn [s]
                        (let [[name commit author message] (string/split s #"\|-\|")]
                          {:name    name
                           :commit  commit
                           :author  author
                           :message message})) filtered-lines)]
    branches))


(defn actual-branches-with-hashes [git-ssh path]
  (remote-branches git-ssh path))


(defn version-branches-with-hashes [git-ssh path]
  (let [all-branches (actual-branches-with-hashes git-ssh path)]
    (filter #(utils/released-version? (:name %)) all-branches)))