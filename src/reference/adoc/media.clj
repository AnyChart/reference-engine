(ns reference.adoc.media
  (:require [reference.config :as config]
            [reference.git :as git]
            [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.io :refer [file]]))

(defn- get-all-static [path]
  (map #(clojure.string/replace (.getAbsolutePath %)
                                (clojure.string/re-quote-replacement path)
                                "")
       (filter #(and
                 (re-matches #".*/_media/.*" (.getAbsolutePath %))
                 (not (.isHidden %))
                 (not (.isDirectory %)))
               (file-seq (file path)))))

(defn- dirname [path]
  (clojure.string/replace path #"[^/]+$" ""))

(defn- move-file [base-path version path]
  (let [target-name (clojure.string/replace path #"_media/" "")
        target-path (str config/static-path version "/" target-name)
        target-folder (dirname target-path)]
    (git/run-sh "mkdir" "-p" target-folder)
    (git/run-sh "cp" (str base-path path) target-path)))

(defn move-media [version]
  (info "moving media for" version)
  (let [base-path (str config/versions-path version "/")
        files (get-all-static base-path)]
    (doall (map #(move-file base-path version %) files))))

(defn update-links [description version]
  (if-not (empty? description)
    (clojure.string/replace
     (clojure.string/replace
      (clojure.string/replace description #"_media/" "")
      #"src='/" (str "src='/" version "/"))
     #"src=\"/" (str "src=\"/" version "/"))
    ""))
