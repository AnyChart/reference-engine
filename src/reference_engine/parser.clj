(ns reference-engine.parser)

(def debug-data (atom nil))

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

;; https://github.com/AnyChart/ACDVF/blob/master/src/cartesian/Chart.js

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
  (let [data (filter-private raw)]
    (get-classes data)))

(use 'clojure.pprint)
(binding [*print-right-margin* 60] (pprint (parse @debug-data)))

(pprint @debug-data)
