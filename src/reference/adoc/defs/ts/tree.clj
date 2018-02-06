(ns reference.adoc.defs.ts.tree)


(defn class-by-name [name classes]
  (first (filter #(= (:full-name %) name) classes)))


(defn parent [class classes]
  (class-by-name (first (:extends class)) classes))


(defn parent-names [class classes]
  (let [*names (atom [])]
    (loop [parent-class (parent class classes)]
      (when parent-class
        (swap! *names conj (:full-name parent-class))
        (recur (parent parent-class classes))))
    @*names))


(defn need-replace? [type parent-names]
  (some #(= type %) parent-names))


(defn update-self-methods [class-name class-methods parent-names]
  (map (fn [method]
         (update method :overrides
                 (fn [overrides]
                   (map (fn [override]
                          (update override :returns
                                  (fn [returns]
                                    (map (fn [return]
                                           (update return :types
                                                   (fn [types]
                                                     (map (fn [type]
                                                            (if (need-replace? type parent-names)
                                                              class-name
                                                              type))
                                                          types))))
                                         returns))))
                        overrides))))
       class-methods))


(defn combine-methods [methods parent-methods]
  (let [new-methods (filter (fn [parent-method]
                              (not (some #(= (:name parent-methods)
                                             (:name %))
                                         methods)))
                            parent-methods)]
    new-methods))


(defn update-methods [class parent-class classes]
  (let [class-methods (:methods class)
        ;; parent-methods (:methods parent-class)
        parent-names (parent-names class classes)

        all-methods (concat
                      class-methods
                      (combine-methods class-methods (:methods parent-class)))

        self-methods (update-self-methods (:full-name class) all-methods parent-names)]
    (assoc class :methods self-methods)))


(defn build-class [class classes *cache]
  (or
    ; get from cache
    (get @*cache (:full-name class))
    ; or build
    (let [parent-class (parent class classes)
          parent-class (when parent-class
                         (build-class parent-class classes *cache))
          new-class (if parent-class
                      (update-methods class parent-class classes)
                      class)]
      (swap! *cache assoc (:full-name class) new-class)
      new-class)))


(defn build-methods [classes]
  (let [*cache (atom {})]
    (map #(build-class % classes *cache) classes)))


(defn modify [tree]
  (update tree :classes build-methods))


;; (reference.adoc.defs.ts.tree/class-by-name "anychart.core.SeparateChart" (:classes (reference.adoc.defs.ts.tree/t0)))
;; (count (:methods (reference.adoc.defs.ts.tree/class-by-name "anychart.core.SeparateChart" (:classes (reference.adoc.defs.ts.tree/t0)))))

;(defn t0 []
;  (let [tree (modify @reference.adoc.defs.typescript/top-level)]
;    tree))


;(defn t []
;  (let [classes (:classes @reference.adoc.defs.typescript/top-level)]
;    (parent-names (class-by-name "anychart.core.SeparateChart" classes) classes)))