(ns reference.adoc.categories
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- get-method-category [method]
  (set (doall (reduce (fn [res override]
                        (if (:has-category override)
                          (conj res (:category override))
                          res)) [] (:overrides method)))))

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

(defn- categorize-namespace-functions [members]
  (doall (map (fn [[k v]] v)
              (reduce (fn [res member]
                        (let [category (if (:has-category member)
                                         (:category member)
                                         "Uncategorized")
                              data (get res category {:name category :members []})]
                          (if (some #(= (:name %) (:name member)) (:members data))
                            res
                            (assoc res category (assoc data
                                                       :members (conj (:members data) member))))))
                      {}
                      members))))

(defn- group-members-by-name [members]
  (doall (reduce (fn [res member]
                   (let [name (:name member)
                         group (get res name [])]
                     (assoc res name (conj group member))))
                 {} members)))

(defn- find-member-with-short-description [group]
  (if-let [member (first (filter #(:has-short-description %) group))]
    member
    (first group)))

(defn- find-category-short-descriptions [category]
  (let [grouped-members (group-members-by-name (:members category))]
    (assoc category :members
           (reduce (fn [res [name group]]
                     (conj res (find-member-with-short-description group)))
                   [] grouped-members))))

(defn- update-class-methods-categories [categories]
  (map find-category-short-descriptions categories))

(defn- categorize-class-methods [methods]
  (update-class-methods-categories
   (doall (map (fn [[k v]] v)
               (reduce (fn [res method]
                         (reduce (fn [res override]
                                   (let [category (if (:has-category override)
                                                    (:category override)
                                                    "Uncategorized")
                                         data (get res category {:name category :members []})]
                                     (assoc res category
                                            (assoc data :members
                                                   (conj (:members data) override)))))
                                 res
                                 (:overrides method)))
                       {}
                       methods)))))

(defn build-class-categories [class]
  (let [categories (get-class-categories (:methods class))
        has-categories (boolean (seq categories))]
    (assoc class
           :categories (if has-categories
                         (categorize-class-methods (:methods class)))
           :has-categories has-categories)))

(defn build-namespace-categories [namespace]
  (let [categories (get-namespace-categories (:functions namespace))
        has-categories (boolean (seq categories))]
    (assoc namespace
           :categories (if has-categories
                         (categorize-namespace-functions (:functions namespace)))
           :has-categories has-categories)))
