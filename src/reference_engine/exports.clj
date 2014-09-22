(ns reference-engine.exports
  (:require [clojure.java.io :refer [file]]
            [clojure.string :refer [split] :as str]))

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
  (let [content (slurp f)
        exports (split
                 (str (last (re-find #"(?s)//exports[\s]*(.*)$" content)))
                 #"\n")]
    (filter #(not (= % nil)) (map parse-export exports))))

(defn generate-exports [path]
  (apply concat (filter #(not (= % nil)) (map get-exports (get-files path)))))

(defn substring? [sub st]
 (not= (.indexOf st sub) -1))

(defn cleanup-name [name]
  (clojure.string/replace (str name) #"['\"]" ""))

(defn filter-exports [data path]
  (let [exports (vec (generate-exports path))
        exports-string (clojure.string/join " " exports)]
    (filter (fn [meta]
              (case (:kind meta)
                "class" (substring? (str longname ".prototype.") exports-string)
                "namespace" (substring? (str longname ".") exports-string)
                true))
            data)))
