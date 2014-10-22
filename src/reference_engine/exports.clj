(ns reference-engine.exports
  (:require [clojure.java.io :refer [file]]
            [clojure.string :refer [split] :as str]
            [reference-engine.parser.utils :as utils]
            [reference-engine.parser.inheritance :as inheritance]))

(defn get-extension [f]
  (last (split (.getName f) #"\.")))

(defn get-files [path]
  (map #(.getAbsolutePath %)
       (filter #(= (get-extension %) "js")
               (file-seq (file path)))))

(defn create-cache []
  (atom {}))

(defn cleanup-cache [cache]
  (reset! cache {}))

(defn cache-info [longname res cache]
  (swap! cache assoc longname res)
  res)

(defn cached-info [longname cache]
  (get @cache longname))

(defn get-exports [f]
  (str (last (re-find #"(?s)//exports[\s]*(.*)$" (slurp f)))))

(defn generate-graphics-exports [path]
  (clojure.string/replace (slurp (str path))
                          "acgraph"
                          "anychart.graphics"))

(defn generate-exports [path]
  (println "searching for exports")
  (let [main-exports (reduce str (pmap get-exports (get-files (str path "/src"))))]
    (if (.exists (file (str path "/contrib/graphics")))
      (str main-exports (generate-graphics-exports (str path "/contrib/graphics/src/export.js")))
      main-exports)))

(defn substring? [sub st]
 (not= (.indexOf st sub) -1))

(defn exported-by-name? [longname name memberof exports cache]
  (if (contains? @cache longname)
    (cached-info longname cache)
    (cache-info longname
                (or (substring? (str longname ";") exports)
                    (substring? (str "'" longname "'") exports)
                    (substring? (str longname ".") exports)
                    (substring? (str memberof ".prototype." name ";") exports))
                cache)))

;(def exported-by-name? (memoize exported-by-name?))

(defn exported? [meta exports cache]
  (exported-by-name? (:longname meta)
                     (:name meta)
                     (:memberof meta)
                     exports
                     cache))

(defn check-exports [obj raw-data exports cache inheritance-cache]
  (if (exported? obj exports cache)
    true
    (if (= (:kind obj) "function")
      (let [name (:name obj)
            container (inheritance/get-class obj raw-data inheritance-cache)
            parents (inheritance/get-all-parents container raw-data inheritance-cache)]
        (some (fn [parent-obj]
                (exported-by-name? (str (:longname parent-obj) "." name)
                                   name
                                   (:longname parent-obj)
                                   exports
                                   cache)) parents)))))
