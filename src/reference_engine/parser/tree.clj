(ns reference-engine.parser.tree)

(defn build-base [node]
  {:kind (:kind node)
   :name (:name node)
   :full-name (:full-name node)})

(declare build-tree-node)

(defn build-tree-node-from-grouped [node]
  (let [members (:members node)]
    (if (seq members)
      (build-tree-node (first members)))))

(defn build-ns-tree-node [node]
  (assoc (build-base node) :children
         (remove #(nil? %)
                 (concat
                  (map build-tree-node (:classes node))
                  (map build-tree-node (:constants node))
                  (map build-tree-node (:fields node))
                  (map build-tree-node-from-grouped (:functions node))
                  (map build-tree-node (:typedefs node))
                  (map build-tree-node (:enums node))))))

(defn build-class-tree-node [node]
  (assoc (build-base node) :children
         (remove #(nil? %)
                 (concat
                  (map build-tree-node-from-grouped (:methods node))
                  (map build-tree-node-from-grouped (:static-methods node))
                  (map build-tree-node (:consts node))
                  (map build-tree-node (:fields node))
                  (map build-tree-node (:static-fields node))))))

(defn build-tree-node [node]
  (case (:kind node)
    "namespace" (build-ns-tree-node node)
    "class" (build-class-tree-node node)
    "enum" (build-base node)
    "typedef" (build-base node)
    "constant" (build-base node)
    "function" (build-base node)
    "field" (build-base node)
    nil))

(defn create-tree [namespaces]
  (doall (pmap build-tree-node namespaces)))
