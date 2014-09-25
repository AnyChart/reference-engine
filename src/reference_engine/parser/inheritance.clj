(ns reference-engine.parser.inheritance)

(def cache (atom {}))
(def name-cache (atom {}))

(defn reset-cache []
  (reset! cache {})
  (reset! name-cache {}))

(defn cache-parents [name parents]
  (swap! cache assoc name parents))

(defn cached-parents [name]
  (get @cache name))

(defn get-obj-by-longname [name raw-data]
  (if (contains? @name-cache name)
    (get @name-cache name)
    (let [res (first (filter #(= (:longname %) name) raw-data))]
      (swap! name-cache assoc name res)
      res)))

(defn get-all-parents [raw-obj raw-data]
  (if (= (:kind raw-obj) "class")
    (if (contains? @cache (:longname raw-obj))
      (cached-parents (:longname raw-obj))
      (let [parents
            (apply concat
                   (map
                    (fn [parent]
                      (let [parent-obj (get-obj-by-longname parent raw-data)]
                        (concat [parent-obj] (get-all-parents parent-obj raw-data))))
                    (:augments raw-obj)))]
        (cache-parents (:longname raw-obj) parents)
        parents))))

(defn get-class [raw-obj raw-data]
  (if (= (:kind raw-obj) "class")
    raw-obj
    (get-obj-by-longname (:memberof raw-obj) raw-data)))
