(ns reference.generator.struct
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- reduce-by-kind
  ([] {})
  ([res entry]
     (let [kind (:kind entry)
           name (:full-name entry)
           container-key (case kind
                       "namespace" :namespaces
                       "class" :classes
                       "typedef" :typedefs
                       "function" :functions
                       "enum" :enums
                       "constant" :constants
                       "field" :fields)
           container (get res container-key)
           entries (get container name)]
       (if entries
         (assoc-in res [container-key name] (conj entries entry))
         (assoc-in res [container-key name] [entry])))))

(defn- group-by-kind [entries]
  (info "group-by-kind" (count entries))
  (reduce reduce-by-kind {:namespaces {}
                          :classes {}
                          :typedefs {}
                          :functions {}
                          :enums {}
                          :constants {}
                          :fields {}} entries))

(defn- members [name struct group static]
  (filter #(if static
             (:static %)
             (not (:static %)))
          (filter (fn [[entry-name entries]]
                    (some #{name} (map :member-of entries)))
                  (get struct group))))

(defn- get-actual-entry [entries]
  (first (sort-by :force-include entries)))

(defn- get-parent-namespace [entry struct]
  (let [member-of (:member-of entry)]
    (if (contains? (:namespaces struct) member-of)
      member-of
      (if-let [parent-class (get-in struct [:classes member-of])]
        (get-parent-namespace (get-actual-entry parent-class) struct)
        member-of))))

(defn- structurize-class [[name entries] struct]
  (let [entry (get-actual-entry entries)]
    [name (assoc entry
            :member-of (get-parent-namespace entry struct)
            :methods (members name struct :functions false)
            :static-methods (members name struct :functions true)
            :constants (members name struct :constants true)
            :fields (members name struct :fields false)
            :static-fields (members name struct :fields true))]))

(defn- get-inheritance [entry classes cache]
  (if (contains? @cache (:full-name entry))
    (get @cache (:full-name entry))
    (let [inherits
          (if (:has-inherits entry)
            (concat (:inherits entry)
                    (reduce concat []
                            (map #(get-inheritance (get classes %) classes cache)
                                 (:inherits entry))))
            nil)]
      (swap! cache assoc (:full-name entry) inherits)
      inherits)))

(defn- check-inheritance [[name entry] classes cache]
  [name (assoc entry :inherits-list (get-inheritance entry classes cache))])

(defn- merge-inherited-methods [entry classes cache]
  (if (contains? @cache (:full-name entry))
    (get @cache (:full-name entry))
    (let [methods (filter (fn [[mname mentries]]
                            (not-any? :inherit-doc mentries))
                          (:methods entry))
          parent-methods (reduce merge
                                 (map #(merge-inherited-methods (get classes %)
                                                                classes
                                                                cache)
                                      (:inherits entry)))
          methods (merge parent-methods methods)]
      (swap! cache assoc (:full-name entry) methods)
      methods)))

(defn- merge-inheritance [[name entry] classes cache]
  [name (assoc entry :methods (merge-inherited-methods entry classes cache))])

(defn- get-children-inheritance [entry classes cache]
  (let [name (:full-name entry)]
    (if (contains? @cache name)
      (get @cache name)
      (let [children-classes (map last (filter
                                        (fn [[cname c]]
                                          (some #{name} (:inherits-list c)))
                                        classes))]
        (concat (map :full-name children-classes)
                (reduce concat
                        []
                        (map (fn [c] (get-children-inheritance c classes cache))
                             children-classes)))))))

(defn- check-children-inheritance [[name entry] classes cache]
  [name (assoc entry :children-list (get-children-inheritance entry classes cache))])

(defn- structurize-enum [[name entries] struct]
  (let [entry (get-actual-entry entries)]
    (if-not (:linked entry)
      entry
      (let [linked-enum (get-actual-entry (get-in struct [:enums (:linked-to entry)]))]
        (assoc entry
          :type (:type linked-enum)
          :has-type (:has-type linked-enum)
          :fields (:fields linked-enum)
          :has-fields (:has-fields linked-enum))))))

(defn- structurize-typedef [[name entries] struct]
  (get-actual-entry entries))

(defn- structurize-namespace [[name entries] struct classes enums typedefs]
  (let [entry (get-actual-entry entries)]
    [name (assoc entry
            :constants (members name struct :constants false)
            :fields (members name struct :fields false)
            :functions (members name struct :functions false)
            :enums (filter #(= (:member-of %) name) enums)
            :typedefs (filter #(= (:member-of %) name) typedefs)
            :classes (filter #(= (:member-of (last %)) name) classes))]))

(defn- structurize [struct]
  (let [inheritance-cache (atom {})
        child-cache (atom {})
        methods-inheritance-cache (atom {})
        classes-struct-base (into {} (map #(structurize-class % struct) (:classes struct)))
        classes-struct-inh (into {} (map #(check-inheritance %
                                                             classes-struct-base
                                                             inheritance-cache)
                                         classes-struct-base))
        classes-struct-parent (into {} (map #(check-children-inheritance
                                              %
                                              classes-struct-inh
                                              child-cache)
                                            classes-struct-inh))
        classes-struct (map #(merge-inheritance %
                                                classes-struct-parent
                                                methods-inheritance-cache)
                            classes-struct-parent)
        enums-struct (map #(structurize-enum % struct) (:enums struct))
        typedefs-struct (map #(structurize-typedef % struct) (:typedefs struct))
        namespaces-struct (map
                           #(structurize-namespace %
                                                   struct
                                                   classes-struct
                                                   enums-struct
                                                   typedefs-struct)
                           (:namespaces struct))]
    (info "classes" (count classes-struct))
    (info "namespaces" (count namespaces-struct))
    (info "typedefs" (count typedefs-struct))
    {:classes (into {} classes-struct)
     :typedefs typedefs-struct
     :enums enums-struct
     :namespaces namespaces-struct}))

(defn build [entries]
  (-> (group-by-kind entries)
      structurize))
