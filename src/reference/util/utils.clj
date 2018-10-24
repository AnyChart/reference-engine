(ns reference.util.utils
  (:require [clojure.string :as string]))


(defn deep-merge [a b]
  (merge-with (fn [x y]
                (cond (map? y) (deep-merge x y)
                      (vector? y) (concat x y)
                      :else y))
              a b))


(defn format-exception [e]
  (str e "\n\n" (apply str (interpose "\n" (.getStackTrace e)))))


(defn released-version? [version-key]
  (or (re-matches #"^\d+\.\d+\.\d+$" version-key)
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