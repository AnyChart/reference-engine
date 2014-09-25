(ns reference-engine.parser.inheritance)

(defn get-obj-by-longname [name raw-data]
  (first (filter #(= (:longname %) name) raw-data)))

(defn get-all-parents [raw-obj raw-data]
  (if (= (:kind raw-obj) "class")
    (apply concat
           (map
            (fn [parent]
              (let [parent-obj (get-obj-by-longname parent raw-data)]
                (concat [parent-obj] (get-all-parents parent-obj raw-data))))
            (:augments raw-obj)))))

(def get-all-parents (memoize get-all-parents))

(defn get-class [raw-obj raw-data]
  (if (= (:kind raw-obj) "class")
    raw-obj
    (get-obj-by-longname (:memberof raw-obj) raw-data)))
