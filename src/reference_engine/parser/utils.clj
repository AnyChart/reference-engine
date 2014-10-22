(ns reference-engine.parser.utils
  (:require [clojure.string :refer [trim]]
            [clojure.tools.logging :as log]
            [reference-engine.config :refer [is-local]]
            [reference-engine.db :refer (wcar*)]))

(def top-level-entries (atom {}))

(defn cache-entry [entry]
  (swap! top-level-entries assoc (:full-name entry) entry)
  entry)

(defn cached-entry [name]
  (get @top-level-entries name))

(defn cleanup-cache []
  (reset! top-level-entries {}))

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

(defn scope-instance? [raw]
  (= (:scope raw) "instance"))

(defn parse-description [description links-prefix]
  (if description
    (clojure.string/replace description
                            #"(?s)\{@link ([^ ]+)\}"
                            (str "<a class=\"type-link\" href=\"" links-prefix "$1\">$1</a>"))))

(defn parse-general-doclet [member sample-callback links-prefix]
  (let [samples (if (and (> (count (:examples member)) 0)
                         (not (= (:examples member) "<CircularRef>")))
                  (doall (map #(sample-callback (cleanup-name (:longname member)) %)
                              (:examples member)))
                  nil)]
    {:name (:name member)
     :description (parse-description (:description member) links-prefix)
     :full-name (cleanup-name (:longname member))
     :examples samples
     :has-examples (> (count samples) 0)
     :illustrations (get-tag member "illustration")
     :file (str (get-in member [:meta :path])
                "/"
                (get-in member [:meta :filename]))}))

(defn filter-members [member raw-data criteria]
  (filter #(and (= (:memberof %) (:full-name member))
                (criteria %))
          raw-data))

(defn group-members [members]
  (let [names (set (map :name members))]
    (map
     (fn [name]
       (let [actual (filter (fn [member] (= (:name member) name)) members)]
         {:name name
          :params-signature (:params-signature (first actual))
          :members actual}))
     names)))

(defn parse-members-with-filter [member raw-data filter-criteria parser]
  (map parser (filter-members member raw-data filter-criteria)))

(defn parse-members-with-filter-to-obj [member raw-data filter-criteria parser fname]
  (let [res (parse-members-with-filter member raw-data filter-criteria parser)
        has-flag-name (keyword (str "has-" (name fname)))]
    {fname res
     has-flag-name (> (count res) 0)}))

(defn parse-grouped-members [member raw-data filter-criteria parser]
  (group-members (parse-members-with-filter member raw-data filter-criteria parser)))

(defn parse-grouped-members-to-obj [member raw-data filter-criteria parser fname]
  (let [res (parse-grouped-members member raw-data filter-criteria parser)
        has-flag-name (keyword (str "has-" (name fname)))]
    {fname res
     has-flag-name (> (count res) 0)}))
