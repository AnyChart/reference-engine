(ns reference.adoc.inheritance
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- class-by-name [name classes]
  (first (filter #(= (:full-name %) name) classes)))

(defn- inherit-method? [method class-methods-names]
  (not (some #(= (:name method) %) class-methods-names)))

(defn- get-inherited-methods [parent-class class-methods-names classes]
  ;;(info "get-inherited-methods" (:full-name parent-class))
  (if parent-class
    (let [methods (:methods parent-class)
          parent-class-name (:full-name parent-class)
          parent-parent-class-name (first (:extends parent-class))
          inherited-methods (filter #(inherit-method? % class-methods-names) methods)]
      (concat (map #(assoc %
                           :inherited-from parent-class-name
                           :is-inherited true) inherited-methods)
              (get-inherited-methods (class-by-name parent-parent-class-name classes)
                                     (concat class-methods-names
                                             (map :name inherited-methods))
                                     classes)))
    []))

(defn- merge-methods [class]
  (let [inherited (:inherited-methods class)
        self (:methods class)]
    (assoc (dissoc class :inherited-methods)
           :methods (sort-by :name (reduce conj self inherited)))))

(defn- build-class-inheritance [class classes]
  ;;(info "build-class-inheritance" (:full-name class) "from" (first (:extends class)))
  (let [class-methods (:methods class)
        class-methods-names (map :name class-methods)
        parent-class-base-name (first (:extends class))
        parent-class-name (if (= (:full-name class) parent-class-base-name)
                            nil
                            parent-class-base-name)
        parent-class (class-by-name parent-class-name classes)
        parent-class-methods (get-inherited-methods parent-class class-methods-names classes)]
    (assoc class :inherited-methods (sort-by :name parent-class-methods))))

(defn build-inheritance [classes]
  (info "build-inheritance")
  (map merge-methods (map #(build-class-inheritance % classes) classes)))
