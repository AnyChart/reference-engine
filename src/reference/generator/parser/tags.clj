(ns reference.generator.parser.tags)

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
