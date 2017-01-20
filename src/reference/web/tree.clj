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

(defn tree-view-full [el version]
  (if (is-group el)
    (str "<li class='pull-down group' x-data-name='" (:full-name el) "'><a href='/" version "/" (:full-name el) "'><i class='ac ac-chevron-right'></i> " (node-title el) "</a><ul style='display:none'>" (reduce str (map #(tree-view-full % version) (:children el))) "</ul></li>")
    (str "<li class='item' x-data-name='" (:full-name el) "'> <a href='/" version "/" (:full-name el) "'>" (node-title el) "</a></li>")))

(defn expand [el parts version]
  (let [first-parts (butlast parts)
        last-part (last parts)]
    (if (some (partial = (:full-name el)) first-parts)
      (when (is-group el)
        (str "<li class='pull-down group' x-data-name='" (:full-name el) "'><a href='/" version "/"
             (:full-name el) "'><i class='ac ac-chevron-down'></i> " (node-title el) "</a><ul>"
             (reduce str (map #(expand % parts version) (:children el))) "</ul></li>"))
      (if (= (:full-name el) last-part)
        (if (is-group el)
          (str "<li class='pull-down group' x-data-name='" (:full-name el) "'><a href='/" version "/"
               (:full-name el) "'><i class='ac ac-chevron-down'></i> " (node-title el) "</a><ul>"
               (reduce str (map #(expand % parts version) (:children el))) "</ul></li>")
          (str "<li class='item' x-data-name='" (:full-name el) "'> <a href='/" version "/" (:full-name el) "'>" (node-title el) "</a></li>"))
        (if (is-group el)
          (str "<li class='pull-down group' x-data-name='" (:full-name el) "'><a href='/" version "/"
               (:full-name el) "'><i class='ac ac-chevron-right'></i> " (node-title el) "</a></li>")
          (str "<li class='item' x-data-name='" (:full-name el) "'> <a href='/" version "/" (:full-name el) "'>" (node-title el) "</a></li>"))))))

(defn tree-view-partial [el version url]
  (let [parts (clojure.string/split url #"\.")
        parts* (reduce (fn [result part]
                         (conj result (str (when (last result) (str (last result) "." )) part)))
                       [] parts)]
    (expand el parts* version)))