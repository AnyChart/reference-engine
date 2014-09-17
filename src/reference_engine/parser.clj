(ns reference-engine.parser)

(defn filter-private [raw]
  (filter (fn [info] (not (= (:access info) "private"))) raw))

(defn parse-class [class]
  {:name (:name class)
   :full-name (:longname class)
   :package (:memberof class)
   :description (:description class)
   :extends (:augments class)})

(defn ignore-circular [obj]
  (if (= obj "<CircularRef>")
    nil
    obj))

(defn parse-package [pkg]
  (:longname pkg))

(defn class-methods [data class]
  (filter #(and (= (:memberof %) (:full-name class))
                (= (:kind %) "function"))
          data))

(defn class-props [data class]
  (filter #(and (= (:memberof %) (:full-name class))
                (= (:kind %) "member"))
          data))

(defn get-classes [data]
  (map parse-class (filter #(= (:kind %) "class") data)))

(defn get-packages [data]
  (set (map :longname (filter #(= (:kind %) "namespace") data))))

(defn parse [raw]
  (let [data (filter-private raw)
        classes (get-classes data)]
    {:classes (map :full-name classes)
     :packages (get-packages raw)}))
