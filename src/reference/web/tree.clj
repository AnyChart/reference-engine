(ns reference.web.tree)

(defn- node-title [el]
  (case (:kind el)
    "function" (str (:name el) "()")
    "method" (str (:name el) "()")
    "enum" (str "[" (:name el) "]")
    "typedef" (str "{" (:name el) "}")
    (:name el)))

(defn- is-group [el]
  (or (= (:kind el) "class")
      (= (:kind el) "namespace")))

(defn tree-view [el version]
  (if (is-group el)
    (str "<li class='pull-down group' x-data-name='" (:full-name el) "'><a href='/" version "/" (:full-name el) "'><i class='fa fa-chevron-right'></i> " (node-title el) "</a><ul style='display:none'>" (reduce str (map #(tree-view % version) (:children el))) "</ul></li>")
    (str "<li class='item' x-data-name='" (:full-name el) "'> <a href='/" version "/" (:full-name el) "'>" (node-title el) "</a></li>")))
