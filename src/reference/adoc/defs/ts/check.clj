(ns reference.adoc.defs.ts.check
  (:require [clojure.java.shell :as shell]
            [reference.git :as git]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(defn check-ts-file [file-name]
  (timbre/info "TypeScript check: tsc")
  (let [{:keys [exit out err] :as result} (shell/sh "/bin/bash" "-c" (str " tsc --noImplicitAny " file-name))]
    (timbre/info result)
    result))


(defn run-tests [dir]
  (let [files (file-seq (io/file dir))
        files (filter (fn [file]
                        (and (not (string/includes? (.getAbsolutePath file) "/.git"))
                             (string/ends-with? (.getName file) ".ts")
                             (not (string/ends-with? (.getName file) ".d.ts"))))
                      files)
        result (doall (pmap (fn [file]
                              (shell/sh "/bin/bash" "-c" (str " tsc --noEmit true \"" file "\"")))
                            files))
        result (filter (fn [res] (not (zero? (:exit res)))) result)]
    (if (seq result)
      {:exit  -1
       :out   (string/join "\n" (map :out result))
       :count (count result)
       :err   ""}
      {:exit 0 :out "" :err ""})))


(defn check-prepare [data-dir version-key git-ssh]
  (timbre/info "TypeScript ts-tests check preparation")
  (let [tests-dir (str data-dir "/ts-tests")
        dir (str data-dir "/ts-tests-" version-key)]
    (git/update git-ssh tests-dir)
    (git/pull git-ssh tests-dir)
    (git/clean git-ssh tests-dir)
    (shell/with-sh-dir tests-dir
                       (shell/sh "/bin/bash" "-c" (str " npm install")))
    (fs/copy-dir tests-dir dir)))


(defn check-clean [data-dir version-key]
  (timbre/info "TypeScript ts-tests check cleaning")
  (let [dir (str data-dir "/ts-tests-" version-key)]
    (fs/delete-dir dir)))


(defn check-graphics [{:keys [path]} data-dir version-key]
  (let [dir (str data-dir "/ts-tests-" version-key)
        res (check-ts-file path)]
    (if (zero? (:exit res))
      (do (fs/copy path (str dir "/graphics/graphics-develop.d.ts"))
          (run-tests (str dir "/graphics")))
      res)))


(defn check-index [{:keys [path]} data-dir version-key]
  (let [dir (str data-dir "/ts-tests-" version-key)
        res (check-ts-file path)]
    (if (zero? (:exit res))
      (do (fs/copy path (str dir "/anychart/index-develop.d.ts"))
          (run-tests (str dir "/anychart")))
      res)))