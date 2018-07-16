(ns reference.adoc.defs.ts.tree
  (:require [com.rpl.specter :refer :all]))


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
  (transform [ALL :overrides ALL :returns ALL :types ALL]
             (fn [type]
               (if (need-replace? type parent-names)
                 class-name
                 type))
             class-methods))


(defn new-methods-from-parent [methods parent-methods]
  (let [new-methods (filter (fn [parent-method]
                              (not (some #(= (:name parent-method)
                                             (:name %))
                                         methods)))
                            parent-methods)]
    new-methods))


(defn update-methods [class parent-class classes add-parent-methods?]
  (let [class-methods (:methods class)
        ;; parent-methods (:methods parent-class)
        parent-names (parent-names class classes)

        all-methods (concat
                      class-methods
                      (when add-parent-methods?
                        (new-methods-from-parent class-methods (:methods parent-class))))

        self-methods (update-self-methods (:full-name class) all-methods parent-names)]
    (assoc class :methods self-methods)))


(defn build-class [class classes *cache add-parent-methods?]
  (or
    ; get from cache
    (get @*cache (:full-name class))
    ; or build
    (let [parent-class (parent class classes)
          parent-class (when parent-class
                         (build-class parent-class classes *cache add-parent-methods?))
          new-class (if parent-class
                      (update-methods class parent-class classes add-parent-methods?)
                      class)]
      (swap! *cache assoc (:full-name class) new-class)
      new-class)))


(defn build-methods [classes add-parent-methods?]
  (let [*cache (atom {})]
    (map #(build-class % classes *cache add-parent-methods?) classes)))


;; add inherited methods from parents, if it needs
;; and replace returned type: e.g.  /anychart.charts.Pie#title - anychart.charts.Pie instead of anychart.core.Chart
(defn modify [tree add-parent-methods?]
  (update tree :classes build-methods add-parent-methods?))


;; (reference.adoc.defs.ts.tree/class-by-name "anychart.core.SeparateChart" (:classes (reference.adoc.defs.ts.tree/t0)))
;; (count (:methods (reference.adoc.defs.ts.tree/class-by-name "anychart.core.SeparateChart" (:classes (reference.adoc.defs.ts.tree/t0)))))

;(defn t0 []
;  (let [tree (modify @reference.adoc.defs.typescript/top-level)]
;    tree))


;(defn t []
;  (let [classes (:classes @reference.adoc.defs.typescript/top-level)]
;    (parent-names (class-by-name "anychart.core.SeparateChart" classes) classes)))