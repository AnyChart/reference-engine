(ns reference.generator.exports
  (:require [clojure.java.io :refer [file]]))

(defn- get-extension [f]
  (last (clojure.string/split (.getName f) #"\.")))

(defn- get-files [path]
  (map #(.getAbsolutePath %)
       (filter #(= (get-extension %) "js")
               (file-seq (file path)))))

(defn- get-exports [f]
  (str (last (re-find #"(?s)//exports[\s]*(.*)$" (slurp f)))))

(defn add-export-from-folder
  ([path exports]
     (str exports (reduce str (map get-exports (get-files path)))))
  ([path] (add-export-from-folder path nil)))

(defn add-exports-from-file [path exports]
  (str exports (slurp path)))

(defn- substring? [sub st]
  (and (not (nil? sub))
       (not (nil? st))
       (not= (.indexOf st sub) -1)))

(defn- check-simple-export [name exports]
  (substring? name exports))

(defn- filter-class-method [[method-name method-entry] entry classes exports cache]
  (let [key (:full-name method-entry)]
    (if (contains? @cache key)
      (get @cache key)
      (let [class-name (:full-name entry)
            res (if (or (substring? key exports)
                        (substring? (str class-name ".prototype." method-name) exports))
                  true
                  (let [parent-class-names (:inherits entry)
                        parent-classes (map #(get classes %) parent-class-names)]
                    (some (fn [centry]
                            (filter-class-method
                             [method-name (get-in centry [:methods method-name])]
                             centry classes exports cache))
                          parent-classes)))]
        (swap! cache assoc key res)
        res))))

(defn- linearize [entries]
  (sort-by :name (map (fn [[name members]]
                        {:name (:name (first members))
                         :members members}) entries)))

(defn- check-class-export [[name entry] exports classes cache]
  (assoc entry
    :static-fields (linearize (filter #(check-simple-export (first %) exports)
                                      (:static-fields entry)))
    :fields (linearize (filter #(check-simple-export (first %) exports) (:fields entry)))
    :consts (linearize (filter #(check-simple-export (first %) exports) (:consts entry)))
    :static-methods (linearize (filter #(check-simple-export (first %) exports)
                                       (:static-methods entry)))
    :methods (linearize (filter #(filter-class-method (first %) entry classes exports cache)
                                (:methods entry)))))

(defn- check-namespace-export [[name entry] exports classes cache]
  (assoc entry
    :enums (filter #(check-simple-export (:full-name %) exports) (:enums entry))
    :typedefs (filter #(check-simple-export (:full-name %) exports) (:typedefs entry))
    :fields (linearize (filter #(check-simple-export (first %) exports) (:fields entry)))
    :constants (linearize (filter #(check-simple-export (first %) exports)
                                  (:constants entry)))
    :functions (linearize (filter #(check-simple-export (first %) exports)
                                  (:functions entry)))
    :classes (map #(check-class-export % exports classes cache)
                  (filter #(check-simple-export (first %) exports) (:classes entry)))))

(defn remove-not-exported [struct exports]
  (let [cache (atom {})]
    (map #(check-namespace-export % exports (:classes struct) cache)
         (filter #(check-simple-export (first %) exports)
                 (:namespaces struct)))))
