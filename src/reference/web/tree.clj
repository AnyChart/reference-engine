(ns reference.web.tree)

(defn- node-title [el]
  (case (:kind el)
    "function" (str (:name el) "()")
    "enum" (str "[" (:name el) "]")
    "typedef" (str "{" (:name el) "}")
    (:name el)))

(defn- is-group [el]
  (or (= (:kind el) "class")
      (= (:kind el) "namespace")))

(defn- node-link [el version])

(defn tree-view [el version]
  (if (is-group el)
    (str "<li class='pull-down group'><a href='/" version "/" (:full-name el) "'><i class='fa fa-chevron-right'></i> " (node-title el) "</a><ul style='display:none'>" (reduce str (map #(tree-view % version) (:children el))) "</ul></li>")
    (str "<li> <a href='/" version "/" (:full-name el) "'>" (node-title el) "</a></li>")))
