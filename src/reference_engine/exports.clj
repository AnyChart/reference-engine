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

(def cache (atom {}))

(defn cleanup-cache []
  (reset! cache {}))

(defn cache-info [longname res]
  (swap! cache assoc longname res)
  res)

(defn cached-info [longname]
  (get @cache longname))

(defn get-exports [f]
  (str (last (re-find #"(?s)//exports[\s]*(.*)$" (slurp f)))))

(defn generate-exports [path]
  (println "searching for exports")
  (time (reduce str (pmap get-exports (get-files path)))))

(defn substring? [sub st]
 (not= (.indexOf st sub) -1))

(defn exported-by-name? [longname name memberof exports]
  (if (contains? @cache longname)
    (cached-info longname)
    (cache-info longname
              (or (substring? longname exports)
                  (substring? (str memberof "['" name "']") exports)
                  (substring? (str memberof "." name) exports)))))

;(def exported-by-name? (memoize exported-by-name?))

(defn exported? [meta exports]
  (exported-by-name? (:longname meta)
                     (:name meta)
                     (:memberof meta) exports))

(defn check-exports [obj raw-data exports]
  (if (exported? obj exports)
    true
    (if (= (:kind obj) "function")
      (let [name (:name obj)
            container (inheritance/get-class obj raw-data)
            parents (inheritance/get-all-parents container raw-data)]
        (some (fn [parent-obj]
                (exported-by-name? (str (:longname parent-obj) "." name)
                                   name
                                   (:longname parent-obj)
                                   exports)) parents)))))
