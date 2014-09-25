(ns reference-engine.exports
  (:require [clojure.java.io :refer [file]]
            [clojure.string :refer [split] :as str]
            [reference-engine.parser.utils :as utils]))

(defn get-extension [f]
  (last (split (.getName f) #"\.")))

(defn get-files [path]
  (map #(.getAbsolutePath %)
       (filter #(= (get-extension %) "js")
               (file-seq (file path)))))

(defn unescape-export [line]
  (str/replace
   (str/replace line #"\[['\"]{1}" ".")
   #"['\"]\s*]" ""))

(defn parse-export [line]
  (let [goog-export (re-matches #"\s*goog\.exportSymbol\(\s*['\"]{1}([^'\"]+).*" line)]
    (if goog-export
      (last goog-export)
      (let [prototype-export (re-matches #"([^\s=]+).*" line)]
        (if prototype-export
          (unescape-export (last prototype-export))
          nil)))))

(defn get-exports [f]
  (str (last (re-find #"(?s)//exports[\s]*(.*)$" (slurp f)))))

(defn generate-exports [path]
  (reduce str (map get-exports (get-files path))))

(defn substring? [sub st]
 (not= (.indexOf st sub) -1))

(defn exported? [meta exports]
  (let [longname (utils/cleanup-name (:longname meta))
        name (utils/cleanup-name (:name meta))
        memberof (utils/cleanup-name (:memberof meta))]
    (or (substring? longname exports)
        (substring? (str memberof "['" name "']") exports)
        (substring? (str memberof "." name) exports))))
