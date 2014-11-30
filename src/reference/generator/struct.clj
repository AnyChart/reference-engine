(ns reference.generator.struct)

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
            :consts (members name struct :constants true)
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
    (let [parent-methods (reduce merge
                                 (map #(merge-inherited-methods (get classes %)
                                                                classes
                                                                cache)
                                      (:inherits entry)))
          methods (merge parent-methods (:methods entry))]
      (swap! cache assoc (:full-name entry) methods)
      methods)))

(defn- merge-inheritance [[name entry] classes cache]
  [name (assoc entry :methods (merge-inherited-methods entry classes cache))])

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

(defn- structurize-namespace [[name entries] struct classes enums]
  (let [entry (get-actual-entry entries)]
    [name (assoc entry
            :constants (members name struct :constants true)
            :fields (members name struct :fields true)
            :functions (members name struct :functions true)
            :enums (filter #(= (:member-of (last %)) name) enums)
            :typedefs (members name struct :typedefs true)
            :classes (filter #(= (:member-of (last %)) name) classes))]))

(defn- structurize [struct]
  (let [inheritance-cache (atom {})
        methods-inheritance-cache (atom {})
        classes-struct-base (into {} (map #(structurize-class % struct) (:classes struct)))
        classes-struct-inh (into {} (map #(check-inheritance %
                                                             classes-struct-base
                                                             inheritance-cache)
                                         classes-struct-base))
        classes-struct (map #(merge-inheritance %
                                                classes-struct-inh
                                                methods-inheritance-cache)
                            classes-struct-inh)
        enums-struct (map #(structurize-enum % struct) (:enums struct))
        namespaces-struct (map
                           #(structurize-namespace % struct classes-struct enums-struct)
                           (:namespaces struct))]
    {:classes (into {} classes-struct)
     :typedefs (map (fn [[name entries]]
                      (get-actual-entry entries))
                    (:typedefs struct))
     :enums enums-struct
     :namespaces namespaces-struct}))

(defn build [entries]
  (-> (group-by-kind entries)
      structurize))
