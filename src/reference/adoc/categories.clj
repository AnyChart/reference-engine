(ns reference.adoc.categories
  (:require [taoensso.timbre :as timbre :refer [info]]
            [com.rpl.specter :refer :all]
            [clojure.java.io :refer [file]]
            [clojure.string :as string]))


(def uncategorized "Miscellaneous")


(defn- assoc-category-id [category]
  (if (:name category)
    (assoc category :id (-> (:name category)
                            (string/replace #" " "-")
                            (string/replace #"/" "-")
                            (.toLowerCase)))
    category))


(defn- get-method-category [method]
  (doall (reduce (fn [res override]
                   (if (:has-category override)
                     (conj res (:category override))
                     res)) #{} (:overrides method))))


(defn- get-class-categories [methods]
  (doall (reduce (fn [res method]
                   (let [categories (filter some? (map get-method-category method))]
                     (concat res categories)))
                 #{} methods)))


(defn- get-namespace-categories [functions]
  (doall (reduce (fn [res func]
                   (if (:has-category func)
                     (conj res (:category func))
                     res))
                 #{} functions)))


(defn- categorize-namespace-functions [members]
  (doall (map (fn [[k v]] v)
              (reduce (fn [res member]
                        (let [category (if (:has-category member)
                                         (:category member)
                                         uncategorized)
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
  (doall (map find-category-short-descriptions categories)))


(defn- add-overrides-categories [overrides]
  (let [categories (reduce (fn [res override]
                             (if (:has-category override)
                               (conj res (:category override))
                               res))
                           #{} overrides)]
    (if-let [category (first categories)]
      (doall (map #(if (:has-category %)
                     %
                     (assoc %
                       :has-category true
                       :category category))
                  overrides))
      overrides)))


(defn- categorize-class-methods [class]
  (update-class-methods-categories
    (doall (map (fn [[k v]] v)
                (reduce (fn [res method]
                          (reduce (fn [res override]
                                    (let [category (if (:has-category override)
                                                     (:category override)
                                                     uncategorized)
                                          data (get res category {:name category :members []})]
                                      (assoc res category
                                                 (assoc data :members
                                                             (conj (:members data) override)))))
                                  res
                                  (add-overrides-categories (:overrides method))))
                        {}
                        (:methods class))))))


(defn- get-categories-order [config]
  (let [raw (string/split-lines config)
        raw-categories (map #(-> %
                                 (string/trim-newline)
                                 (string/trim)) raw)
        categories (filter #(not (string/blank? %)) raw-categories)]
    (doall
      (reduce (fn [res val]
                (assoc res (:name val) (:index val)))
              {}
              (map-indexed (fn [idx category]
                             {:name  category
                              :index idx})
                           categories)))))


(defn parse-categories-order [data-dir branch]
  (info "categories file path" (str data-dir "/versions/" branch "/categories"))
  (-> (if (.exists (file (str data-dir "/versions/" branch "/categories")))
        (get-categories-order (slurp (str data-dir "/versions/" branch "/categories")))
        {})
      (assoc uncategorized 9999999)))




(defn sort-categories [categories sorting]
  (->> categories
       (map #(assoc % :index
                      (case (:id %)
                        "specific-settings" -2
                        "icon-settings" -1
                        (get sorting (:name %) 999998))))
       (sort-by (juxt :index :name))
       (map #(dissoc % :index))))


(defn- sort-members [categories]
  (map #(assoc % :members (sort-by :name (:members %))) categories))


(defn- assoc-categories-id [categories]
  (doall (map assoc-category-id categories)))


(defn build-class-categories [class sorting]
  (let [categories (get-class-categories (:methods class))
        has-categories (boolean (seq categories))]
    (assoc class
      :categories (if has-categories
                    (-> (categorize-class-methods class)
                        (assoc-categories-id)
                        (sort-categories sorting)
                        (sort-members)))
      :has-categories has-categories)))


(defn build-namespace-categories [namespace sorting]
  (let [categories (get-namespace-categories (:functions namespace))
        has-categories (boolean (seq categories))]
    (assoc namespace
      :categories (if has-categories
                    (-> (categorize-namespace-functions (:functions namespace))
                        (assoc-categories-id)
                        (sort-categories sorting)
                        (sort-members)))
      :has-categories has-categories)))


(defn categorize [inh-top-level categories-order]
  (->> inh-top-level
       (transform [:namespaces ALL] #(build-namespace-categories % categories-order))
       (transform [:classes ALL] #(build-class-categories % categories-order))))