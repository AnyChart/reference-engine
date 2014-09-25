(ns reference-engine.parser.utils
  (:require [clojure.string :refer [trim]]
            [clojure.tools.logging :as log]))

(def counter (atom 0))

(defn contains-tag? [raw tag]
  (some #(= (:originalTitle %) tag) (:tags raw)))

(defn force-include? [raw]
  (contains-tag? raw "includeDoc"))

(defn ignore? [raw]
  (contains-tag? raw "ignoreDoc"))

(defn inherit-doc? [raw]
  (contains-tag? raw "inheritDoc"))

(defn get-tag [raw tag]
  (map :value (filter #(= (:originalTitle %) tag) (:tags raw))))

(defn cleanup-name [name]
  (if name
    (clojure.string/replace (str name) #"['\"]" "")
    nil))

(defn static? [raw]
  (= (:scope raw) "static"))

(defn parse-general-doclet [member]
  (swap! counter inc)
  {:name (:name member)
   :description (:description member)
   :full-name (cleanup-name (:longname member))
   :examples (:examples member)
   :illustrations (get-tag member "illustration")})

(defn filter-members [member raw-data criteria]
  (filter #(and (= (:memberof %) (:full-name member))
                (criteria %))
          raw-data))

(defn group-members [members]
  (let [names (set (map :name members))]
    (map
     (fn [name]
       {:name name
        :members (filter (fn [member] (= (:name member) name)) members)})
     names)))

(defn parse-members-with-filter [member raw-data filter-criteria parser]
  (map parser (filter-members member raw-data filter-criteria)))

(defn parse-grouped-members [member raw-data filter-criteria parser]
  (group-members (parse-members-with-filter member raw-data filter-criteria parser)))
