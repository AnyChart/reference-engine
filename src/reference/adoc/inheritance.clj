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


;(defn- merge-methods [class]
;  (let [inherited (:inherited-methods class)
;        methods (:methods class)]
;    (assoc (dissoc class :inherited-methods)
;           :methods (sort-by :name (concat methods inherited)))))


(defn- build-class-inheritance [class classes]
  ;;(info "build-class-inheritance" (:full-name class) "from" (first (:extends class)))
  (let [class-methods (:methods class)
        class-methods-names (map :name class-methods)
        parent-class-base-name (first (:extends class))
        parent-class-name (if (= (:full-name class) parent-class-base-name)
                            nil
                            parent-class-base-name)
        parent-class (class-by-name parent-class-name classes)
        parent-class-methods (get-inherited-methods parent-class class-methods-names classes)
        ;remove inherited ignored methods
        inherited-methods (filter
                            (fn [m] (some #(= (:name m) (:name %)) (:all-members class)))
                            parent-class-methods)
        all-methods (sort-by :name (concat class-methods inherited-methods))
        all-methods-set-inh (map (fn [method]
                                   (if (some #(= (:name method) (:name %)) (:all-members parent-class))
                                     (assoc method
                                       :inherited-from parent-class-name
                                       :is-inherited true)
                                     method))
                                 all-methods)]

    ;(when (= (:full-name class) "anychart.charts.Cartesian")
    ;  (println "CARTESIAN:")
    ;  (prn class)
    ;
    ;  (println "\n\n\nParent class: ")
    ;  (prn parent-class)
    ;
    ;  (println "\n\n\nParent class methods: ")
    ;  (prn parent-class-methods)
    ;
    ;  (println "\n\n\nInherited methods: ")
    ;  (prn inherited-methods)
    ;
    ;  (println "\n\n\nAll methods: ")
    ;  (prn all-methods)
    ;
    ;  (println "\n\n\nAll methods set inh")
    ;  (prn all-methods-set-inh)
    ;  )

    (-> class
        ;(assoc :inherited-methods (sort-by :name filtered-parent-class-methods))
        (dissoc :all-members)
        (assoc :methods all-methods-set-inh))))


(defn- set-default-expand-method-override** [method class-names]
  (let [
        ;; get getter index
        getters (keep-indexed #(when (.startsWith (:description %2) "Getter")
                                 [%1 %2])
                              (:overrides method))

        getter-index (when (= 1 (count getters))
                       (let [[index getter] (first getters)
                             types (mapcat :types (:returns getter))]
                         (when (some #(some (partial = %) class-names) types)
                           index)))

        ;; get default-doclet (@default_doclet from .adoc)
        default-doclet-index (first (keep-indexed #(when (:default-doclet %2) %1)
                                                  (:overrides method)))

        index (or default-doclet-index getter-index)]
    (if index
      (assoc method :default-override-index index)
      method)))


(defn- set-default-expand-method-override* [class class-names]
  (let [methods (:methods class)
        new-methods (map #(set-default-expand-method-override** % class-names) methods)]
    (assoc class :methods new-methods)))


(defn- set-default-expand-method-override [classes]
  (map #(set-default-expand-method-override* % (map :full-name classes)) classes))


(defn update-classes [classes]
  (let [new-classes (map #(build-class-inheritance % classes) classes)]
    (set-default-expand-method-override new-classes)))


(defn build-inheritance [raw-top-level]
  (info "Build inheritance")
  (update raw-top-level :classes update-classes))
