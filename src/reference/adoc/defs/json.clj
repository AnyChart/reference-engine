(ns reference.adoc.defs.json
  (:require [cheshire.core :as ches]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json]))


;; for developing
(defonce top-level (atom nil))

(defn set-top-level! [_top-level]
  (reset! top-level _top-level))

;;======================================================================================================================
;; Getters
;;======================================================================================================================
(defn get-typedefs [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:typedefs top-level)))


(defn get-enums [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:enums top-level)))


(defn get-classes [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:classes top-level)))

;;======================================================================================================================
;; Clean
;;======================================================================================================================
(defn clean [item]
  (dissoc item :since :has-since :last-modified :file :categories
          :has-categories :has-short-description :has-description
          :has-detailed :detailed
          :has-playground-samples :playground-samples
          :has-listings :listings
          :has-categories :categories
          :has-category :category))


(defn clean-typedef [typedef]
  (-> typedef
      (assoc :properties (map clean (:properties typedef)))
      clean))


(defn clean-enum [item]
  (-> item
      (assoc :fields (map clean (:fields item)))
      clean))


(defn clean-class [class]
  (-> class
      (update :methods (fn [methods]
                         (map (fn [method]
                                (update method :overrides (fn [overrides]
                                                            (map clean overrides))))
                              methods)))
      clean))


(defn clean-namespace [class]
  (-> class
      (update :functions (fn [methods]
                           (map (fn [method]
                                  (update method :overrides (fn [overrides]
                                                              (map clean overrides))))
                                methods)))
      clean))


;;======================================================================================================================
;; Main generation
;;======================================================================================================================
(defn namespace-definition [top-level namespace]
  (-> namespace
      (assoc
        :typedefs (map clean-typedef (get-typedefs top-level (map :name (:typedefs namespace))))
        :enums (map clean-enum (get-enums top-level (map :name (:enums namespace))))
        :classes (map clean-class (get-classes top-level (map :name (:classes namespace)))))
      clean-namespace))


(defn optimize-tree [top-level]
  (map #(namespace-definition top-level %)
       (sort-by :full-name (:namespaces top-level))))


(defn optimize-flat [top-level]
  {:classes    (map clean-class (:classes top-level))
   :typedefs   (map clean-typedef (:typedefs top-level))
   :enums      (map clean-enum (:enums top-level))
   :namespaces (map clean-namespace (:namespaces top-level))})


(defn generate-ts [top-level version-key is-last-versionl]
  (ches/generate-string (optimize-flat top-level)))


(defn test2 []
  (let [ts (generate-ts @top-level "develop" false)]
    (spit "/media/ssd/sibental/reference-engine-data/data.json" ts)))


(defn generate [data-dir version-key latest-version-key top-level]
  (timbre/info "generate JSON definitions for: " version-key ", latest: " latest-version-key)
  (let [dir (str data-dir "/versions-static/" version-key)]
    (fs/mkdirs dir)
    (spit (str dir "/anychart.json")
          (ches/generate-string (optimize-flat top-level)))
    (spit (str dir "/anychart-tree.json")
          (ches/generate-string (optimize-tree top-level)))))


; test anychart.json for pie
;(defn t1 []
;  (let [tree (json/parse-string (slurp "/media/ssd/sibental/reference-engine/data/versions-static/develop/anychart.json") true)
;        classes (:classes tree)
;        pie (first (filter #(= (:name %) "Pie") classes))]
;
;    (clojure.pprint/pprint pie)))