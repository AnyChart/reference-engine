(ns reference-engine.samples
  (:require [clojure.string :refer [trim]]
            [reference-engine.git :as git]
            [clojure.java.io :refer [file]]))

(defn update-repo [base-path]
  (git/update (str base-path "/samples")))

(defn parse-sample-local [obj-name sample]
  (let [t-info (last (re-find #"(?s)<t>(.*)</t>" sample))
        c-info (last (re-find #"(?s)<c>(.*)</c>" sample))
        code (trim (last (re-find #"(?s)((<t>(.*)</t>)|(<c>(.*)</c>))*(.*)" sample)))]
    {:listing-only? (= t-info "listingOnly")
     :title c-info
     :code code}))

(defn get-sample-file-name [data-folder fname]
  (if (.exists (file (str data-folder fname ".sample")))
    (loop [index 1]
      (if (not (.exists (file (str data-folder fname "-" index ".sample"))))
        (str fname "-" index)
        (recur (+ index 1))))
    fname))

(defn parse-sample-server [base-path version obj-name sample]
  (let [sample-info (parse-sample-local obj-name sample)
        data-folder (str base-path "/samples-data/" version "/")
        fname (clojure.string/replace obj-name #"#" ".")]
    (if (not (.exists (file data-folder)))
      (git/run-sh "mkdir" data-folder))
    (if (not (:listing-only? sample-info))
      (let [playground-name (get-sample-file-name data-folder fname)]
        (spit (str data-folder fname ".sample") (:code sample-info))
        (assoc sample-info :playground fname))
      sample-info)))

(defn synchronize-repo [base-path versions]
  (let [data-path (str base-path "/samples-data/")
        repo-path (str base-path "/samples")]
    (git/update repo-path)
    (let [branches (.listFiles (file data-path))]
      (println branches)
      (doall (map (fn [branch-f]
                    (let [branch (.getName branch-f)]
                      (println "syncing samples for" branch)
                      (git/run-git repo-path "checkout" "-b" branch)
                      (git/run-git repo-path "checkout" branch)
                      (git/run-sh "rm" "-rf" (str repo-path "/*"))
                      (git/run-sh "cp" "-R" (str data-path branch))
                      (git/run-git repo-path "add" "-A" ".")
                      (git/run-git repo-path "commit" "-m" "autobuild")))
                      ;;(git/run-sh "push" "origin" branch)))
                  (filter #(and (.isDirectory %)
                                (not (.isHidden %))
                                (some (fn [n] (= (.getName %) n)) versions))
                          branches))))))

;;(synchronize-repo "/Users/alex/Work/anychart/reference-engine/data/acdvf" ["RC-7.2.0"])
