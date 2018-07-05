(ns reference.adoc.media
  (:require [reference.git :as git]
            [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.io :refer [file]]
            [clojure.string :as string]))


(defn- get-all-static [path]
  (map #(string/replace (.getAbsolutePath %)
                        (string/re-quote-replacement path)
                        "")
       (filter #(and
                  (re-matches #".*/_media/.*" (.getAbsolutePath %))
                  (not (.isHidden %))
                  (not (.isDirectory %)))
               (file-seq (file path)))))


(defn- dirname [path]
  (string/replace path #"[^/]+$" ""))


(defn- move-file [base-path version path dest-path]
  (let [target-name (-> path
                        (string/replace #"_media/" "")
                        (string/replace (string/re-quote-replacement base-path) ""))
        target-path (str dest-path version "/" target-name)
        target-folder (dirname target-path)]
    (git/run-sh "mkdir" "-p" target-folder)
    (git/run-sh "cp" (str base-path path) target-path)))


(defn move-media [version src-path dest-path]
  (info "moving media for" version "from" src-path "to" dest-path)
  (let [base-path (str src-path version "/")
        files (get-all-static base-path)]
    (doall (map #(move-file base-path version % (str dest-path "/")) files))))


(defn update-links [description version]
  (if-not (empty? description)
    (-> description
        (string/replace #"_media/" "/")
        (string/replace #"src='/" (str "src='/si/" version "/"))
        (string/replace #"src=\"/" (str "src=\"/si/" version "/")))
    ""))
