(ns reference.adoc.inheritance)

(defn- class-by-name [name classes]
  (first (filter #(= (:full-name %) name) classes)))

(defn- get-all-class-methods [class classes]
  (if (:has-extends class)
    (let [parent-name (first (:extends class))
          parent (class-by-name parent-name classes)]
      (map #(assoc % :inherited-from parent-name) (:methods parent)))
    []))

(defn- get-all-class-methods [class classes]
  ())

(defn- add-class-methods [class classes])
