(ns reference.util.utils
  (:require [clojure.string :as string]
            [version-clj.core :refer [version-compare]]))


(defn deep-merge [a b]
  (merge-with (fn [x y]
                (cond (map? y) (deep-merge x y)
                      (vector? y) (concat x y)
                      :else y))
              a b))


(defn format-exception [e]
  (str e "\n\n" (apply str (interpose "\n" (.getStackTrace e)))))


(defn sort-versions
  ([key versions]
   (let [replace-fn (fn [version]
                      ;; v8 -> 8.999.999
                      (string/replace version #"^v(\d+)" "$1.999.999"))
         compare-fn (fn [v1 v2]
                      (version-compare (replace-fn v2) (replace-fn v1)))]
     (sort-by key compare-fn versions)))
  ([versions]
   (sort-versions identity versions)))


(defn released-version? [version-key]
  (or
    ;; (re-matches #"^\d+\.\d+\.\d+$" version-key)
    (re-matches #"^v\d+$" version-key)))


(defn name->url [name]
  (-> name
      ; TODO: refactor with one replace
      (string/replace #"^/" "")
      (string/replace #"/" "-")
      (string/replace #", " "-")
      (string/replace #",_" "-")
      (string/replace #"," "-")
      (string/replace #" " "-")
      (string/replace #"_" "-")
      (string/replace #"\(" "")
      (string/replace #"\)" "")
      string/lower-case))