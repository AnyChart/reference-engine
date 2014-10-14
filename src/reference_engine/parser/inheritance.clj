(ns reference-engine.parser.inheritance)

(defn create-cache []
  {:cache (atom {})
   :name-cache (atom {})
   :classes-parents-cache (atom {})
   :class-by-name (atom {})})

(defn cleanup-cache [cache]
  (reset! (:cache cache) {})
  (reset! (:name-cache cache) {})
  (reset! (:classes-parents-cache cache) {})
  (reset! (:class-by-name cache) {}))

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

(defn cache-class-parents [class classes-parents-cache class-by-name-cache]
  (if (contains? @classes-parents-cache (:full-name class))
    (get @classes-parents-cache (:full-name class))
    (let [parents
          (apply concat (map (fn [parent-name]
                               (let [parent-obj (get @class-by-name-cache parent-name)]
                                 (concat [parent-name] (cache-class-parents parent-obj classes-parents-cache class-by-name-cache))))
                             (:inherits-names class)))]
      (swap! classes-parents-cache assoc (:full-name class) parents)
      parents)))

(defn add-inherited-methods [class-name methods new-methods]
  (doall (reduce (fn [res m]
                   (assoc res
                     (:name m)
                     (assoc m
                       :inherited-from class-name
                       :is-inherited true))) methods new-methods)))

(defn get-inherited-methods [class parents-cache classes-cache]
  (let [parents (reverse (get @parents-cache (:full-name class)))
        inherited-methods (loop [parents parents
                                 acc {}]
                            (if (empty? parents)
                              acc
                              (recur (rest parents)
                                     (add-inherited-methods (first parents) acc (:methods (get @classes-cache (first parents)))))))]
    (assoc class
      :inherited-methods (reduce #(conj %1 (last %2)) [] inherited-methods)
      :has-inherited-methods (not (empty? inherited-methods)))))

(defn inject-inherited-methods [namespaces cache top-level-entry-replacer]
  (let [classes (apply concat (map :classes namespaces))]
    ;; init name -> class cache
    (doall (map (fn [c] (swap! (:class-by-name cache) assoc (:full-name c) c))
                classes))
    
    ;; init parents cache
    (doall (map #(cache-class-parents %
                                      (:classes-parents-cache cache)
                                      (:class-by-name cache))
                classes))

    (let [sorted-classes (sort-by #(count (get @(:classes-parents-cache cache) (:full-name %))) classes)
          updated-classes (doall (map #(get-inherited-methods
                                        %
                                        (:classes-parents-cache cache)
                                        (:class-by-name cache))
                                      sorted-classes))]
      (doall (map #(top-level-entry-replacer %) updated-classes)))
    namespaces))
