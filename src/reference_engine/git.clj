(ns reference-engine.git
  (:require [clojure.java.shell :refer [sh with-sh-env with-sh-dir]]
            [clojure.java.io :refer [file writer]]
            [clojure.tools.logging :as log]
            [clojure.string :refer [split trim]]
            [reference-engine.config :refer [git-ssh]]))

(def env-config {:GIT_SSH git-ssh})

(defn run-sh [& command]
  (with-sh-env env-config
    (apply sh command)))

(defn run-git [path & command]
  (with-sh-env env-config
    (with-sh-dir path
      (let [res (apply sh "/usr/bin/git" command)]
        (println res)
        (:out res)))))

(defn lock-path [path]
  (let [f (file path)]
    (str (.getParent f) "/" (.getName f) ".lock")))

(defn lock [path]
  (with-open [w (writer (lock-path path))]
    (.write w "locked")))

(defn unlock [path]
  (.delete (file (lock-path path))))

(defn locked? [path]
  (.exists (file (lock-path path))))

(defn clone [url path]
  (let [res (run-sh "/usr/bin/git" "clone" url path)]
    (str (:err res) "<br />" (:out res) "<br />Done!")))

(defn do-update [path]
  (lock path)
  ;; should test lock. probably not working correctly :(
  ;; or work in main thread, terrible terrible bug
  (let [res (future (str (run-git path "fetch")))]
    @res
    (run-git path "fetch" "-p")
    (unlock path)
    "Updated"))

(defn update [path]
  (if (locked? path)
    "Repository locked"
    (do-update path)))

(defn get-hash [path]
  (run-git path "rev-parse" "HEAD"))

(defn create-data-folder [path]
  (run-sh "mkdir" "-p" (str (.getParent (file path)) "/data")))

(defn actual-branches [path]
  (map (fn [s] (last (re-matches #".*origin/([^ ]+).*" s)))
       (filter (fn [s] (and (not (= s nil))
                            (not (.contains s "->"))))
               (split (run-git path "branch" "-r") #"\n"))))

(defn version-branches [path]
  (map (fn [s] (re-find #"\d\.\d\.\d" s))
       (filter (fn [s] (and (not (.contains s "->"))
                            (re-matches #"[ ]+origin/\d\.\d\.\d" s)))
               (split (run-git path "branch" "-r") #"\n"))))

(defn checkout-to [path branch-or-tag target]
  (run-sh "rm" "-rf" target)
  (run-sh "cp" "-R" path target)
  (run-git target "checkout" branch-or-tag)
  (run-git target "pull" "origin" branch-or-tag))
