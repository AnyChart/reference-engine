(ns reference-engine.parser.inheritance)

(defn get-all-parents [raw-obj raw-data]
  (apply concat
         (map
          (fn [parent]
            (let [parent-obj (some (filter #(= (:longname %) parent)))]
              (concat [parent-obj] (get-all-parents parent-obj raw-data))))
          (:augments raw-obj))))
