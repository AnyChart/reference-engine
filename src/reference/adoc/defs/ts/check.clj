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
        result (doall (map (fn [file]
                             (shell/sh "/bin/bash" "-c" (str " tsc --noEmit true \"" file "\"")))
                           files))
        result (filter (fn [res] (not (zero? (:exit res)))) result)

        ;result (map (fn [res]
        ;              (update res :out (fn [out]
        ;                                 (string/replace out )
        ;                                 ))
        ;              ) result)

        ]
    (if (seq result)
      {:exit -1
       :out  (string/join "\n" (map :out result))
       :err  ""}
      {:exit 0 :out "" :err ""})))


(defn check-ts-tests [file-name version-key data-dir git-ssh]
  (timbre/info "TypeScript check: ts-tests")
  (let [tests-dir (str data-dir "/ts-tests")
        dir (str data-dir "/ts-tests-" version-key)]
    (git/update git-ssh tests-dir)
    (git/pull git-ssh tests-dir)
    (git/clean git-ssh tests-dir)
    (shell/with-sh-dir tests-dir
                       (shell/sh "/bin/bash" "-c" (str " npm install")))
    (fs/copy-dir tests-dir dir)
    (fs/copy file-name (str dir "/index-develop.d.ts"))
    (let [result (run-tests dir)]
      (fs/delete-dir dir)
      result)))


(defn check [file-name version-key data-dir git-ssh]
  (let [res (check-ts-file file-name)]
    (if (zero? (:exit res))
      (check-ts-tests file-name version-key data-dir git-ssh)
      res)))