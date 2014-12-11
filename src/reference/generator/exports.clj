(ns reference.generator.exports
  (:require [clojure.java.io :refer [file]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- get-extension [f]
  (last (clojure.string/split (.getName f) #"\.")))

(defn- get-files [path]
  (map #(.getAbsolutePath %)
       (filter #(= (get-extension %) "js")
               (file-seq (file path)))))

(defn- get-exports [f]
  (clojure.string/replace 
   (str (last (re-find #"(?s)//exports[\s]*(.*)$" (slurp f))))
   "acgraph"
   "anychart.graphics"))

(defn add-export-from-folder
  ([path exports]
     (info "add-export-from-folder" path)
     (str exports (reduce str (map get-exports (get-files path)))))
  ([path] (add-export-from-folder path nil)))

(defn add-exports-from-file [path exports]
  (info "add-exports-from-file" path)
  (str exports (clojure.string/replace
                (slurp path)
                "acgraph"
                "anychart.graphics")))

(defn- substring? [sub st]
  (and (not (nil? sub))
       (not (nil? st))
       (not= (.indexOf st sub) -1)))

(defn- raw-check [name exports]
  (or (substring? (str "'" name "'") exports)
      (substring? (str "\"" name "\"") exports)))

(defn- check-simple-export [name exports cache]
  (if (contains? @cache name)
    (get @cache name)
    (let [res (or (raw-check name exports)
                  (substring? (str name ".") exports))]
      (swap! cache assoc name res)
      res)))

(defn- check-string-export [name exports cache]
  (if (contains? @cache name)
    (get @cache name)
    (let [res (substring? name exports)]
      (swap! cache assoc name res)
      res)))

(defn- add-exported-method [method class]
  (map (fn [child-class]
         (str child-class ".prototype['" (:name method) "'];\n"))
       (:children-list class)))

(defn- add-exports-for-inheritance [[name class] exports cache]
  (if (check-simple-export name exports cache)
    (apply concat (map (fn [[method-name method-members]]
                         (add-exported-method (first method-members) class))
                       (filter (fn [[method-name method-members]]
                                 (or (check-string-export (str name
                                                               ".prototype['"
                                                               (:name (first method-members))
                                                               "']") exports cache)
                                     (check-string-export (str name
                                                               ".prototype[\""
                                                               (:name (first method-members))
                                                               "\"]")
                                                          exports cache)))
                               (:methods class))))))

(defn- filter-class-method [[method-name method-entry] entry classes exports cache]
  (let [key (:full-name method-entry)]
    (if (contains? @cache key)
      (get @cache key)
      (let [class-name (:full-name entry)
            method-full-name-1 (str class-name ".prototype['" method-name "']")
            method-full-name-2 (str class-name ".prototype[\"" method-name "\"]")
            res (or (check-string-export method-full-name-1 exports cache)
                    (check-string-export method-full-name-2 exports cache))]
        (swap! cache assoc key res)
        res))))

(defn- linearize [entries]
  (filter (fn [entry] (not= nil entry))
          (sort-by :name (map (fn [[name members]]
                                (if (empty? members)
                                  nil
                                  {:name (:name (first members))
                                   :members members})) entries))))

(defn- check-class-export [[name entry] exports classes cache]
  (assoc entry
    :static-fields (linearize (filter #(check-simple-export (first %) exports cache)
                                      (:static-fields entry)))
    :fields (linearize (filter #(check-simple-export (first %) exports cache)
                               (:fields entry)))
    :consts (linearize (filter #(check-simple-export (first %) exports cache)
                               (:consts entry)))
    :static-methods (linearize (filter #(check-simple-export (first %) exports cache)
                                       (:static-methods entry)))
    :methods (linearize (filter #(filter-class-method (first %) entry classes exports cache)
                                (:methods entry)))))

(defn- check-namespace-export [[name entry] exports classes cache]
  (assoc entry
    :enums (filter #(check-simple-export (:full-name %) exports cache) (:enums entry))
    :typedefs (filter #(check-simple-export (:full-name %) exports cache) (:typedefs entry))
    :fields (linearize (filter #(check-simple-export (first %) exports cache) (:fields entry)))
    :constants (linearize (filter #(check-simple-export (first %) exports cache)
                                  (:constants entry)))
    :functions (linearize (filter #(check-simple-export (first %) exports cache)
                                  (:functions entry)))
    :classes (map #(check-class-export % exports classes cache)
                  (filter #(check-simple-export (first %) exports cache) (:classes entry)))))

(defn- update-exports [classes exports cache]
  (apply str (apply concat (map #(add-exports-for-inheritance % exports cache) classes))))

(defn remove-not-exported [struct exports]
  (let [cache (atom {})
        exports (str (update-exports (:classes struct) exports cache) exports)]
    (map #(check-namespace-export % exports (:classes struct) cache)
         (filter #(check-simple-export (first %) exports cache)
                 (:namespaces struct)))))
