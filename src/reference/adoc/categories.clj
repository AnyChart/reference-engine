(ns reference.adoc.categories
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- get-method-category [method]
  (first (reduce (fn [res override]
                   (if (:has-category override)
                     (conj res (:category override))
                     res)) [] (:overrides method))))

(defn- get-class-categories [methods]
  (set (doall (reduce (fn [res method]
                        (let [categories (filter some? (map get-method-category method))]
                          (concat res categories)))
                      [] methods))))

(defn- get-namespace-categories [functions]
  (set (doall (reduce (fn [res func]
                        (if (:has-category func)
                          (conj res (:category func))
                          res))
                      [] functions))))

(defn- categorize-members [members]
  (doall (map (fn [[k v]] v)
              (reduce (fn [res member]
                        (let [category (if (:has-category member)
                                         (:category member)
                                         "Uncategorized")
                              data (get res category {:name category :members []})]
                          (assoc res category (assoc data
                                                     :members (conj (:members data) member)))))
                      {}
                      members))))

(defn build-class-categories [class]
  (let [categories (get-class-categories (:methods class))]
    (assoc class
           :categories categories
           :has-categories (boolean (seq categories)))))

(defn build-namespace-categories [namespace]
  (let [categories (get-namespace-categories (:functions namespace))
        has-categories (boolean (seq categories))]
    (assoc namespace
           :categories (if has-categories
                         (categorize-members (:functions namespace)))
           :has-categories has-categories)))
