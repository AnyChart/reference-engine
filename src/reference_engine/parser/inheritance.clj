(ns reference-engine.parser.inheritance)

(defn create-cache []
  {:cache (atom {})
   :name-cache (atom {})})

(defn cleanup-cache [cache]
  (reset! (:cache cache) {})
  (reset! (:name-cache cache) {}))

(defn cache-parents [name parents cache]
  (swap! (:cache cache) assoc name parents))

(defn cached-parents [name cache]
  (get @(:cache cache) name))

(defn get-obj-by-longname [name raw-data cache]
  (if (contains? @(:name-cache cache) name)
    (get @(:name-cache cache) name)
    (let [res (first (filter #(= (:longname %) name) raw-data))]
      (swap! (:name-cache cache) assoc name res)
      res)))

(defn get-all-parents [raw-obj raw-data cache]
  (if (= (:kind raw-obj) "class")
    (if (contains? @(:cache cache) (:longname raw-obj))
      (cached-parents (:longname raw-obj) cache)
      (let [parents
            (apply concat
                   (map
                    (fn [parent]
                      (let [parent-obj (get-obj-by-longname parent raw-data cache)]
                        (concat [parent-obj] (get-all-parents parent-obj raw-data cache))))
                    (:augments raw-obj)))]
        (cache-parents (:longname raw-obj) parents cache)
        parents))))

(defn get-class [raw-obj raw-data cache]
  (if (= (:kind raw-obj) "class")
    raw-obj
    (get-obj-by-longname (:memberof raw-obj) raw-data cache)))
