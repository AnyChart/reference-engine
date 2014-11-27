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

(defn- check-inheritance [[name entry] classes]
  [name entry]) ; to be added

(defn- structurize-enum [[name entries] struct]
  (let [entry (get-actual-entry entries)]
    [name (if-not (:linked entry)
            entry
            (let [linked-enum (get-actual-entry (get-in struct [:enums (:linked-to entry)]))]
              (assoc entry
                :type (:type linked-enum)
                :has-type (:has-type linked-enum)
                :fields (:fields linked-enum)
                :has-fields (:has-fields linked-enum))))]))

(defn- structurize-namespace [[name entries] struct classes enums]
  (let [entry (get-actual-entry entries)]
    [name (assoc entry
            :constants (members name struct :constants true)
            :fields (members name struct :fields true)
            :functions (members name struct :functions true)
            :enums (filter #(= (:member-of (last %)) name) enums)
            :classes (filter #(= (:member-of (last %)) name) classes))]))

(defn- structurize [struct]
  (let [classes-struct-base (map #(structurize-class % struct) (:classes struct))
        classes-struct (map #(check-inheritance % classes-struct-base) classes-struct-base)
        enums-struct (map #(structurize-enum % struct) (:enums struct))
        namespaces-struct (map
                           #(structurize-namespace % struct classes-struct enums-struct)
                           (:namespaces struct))]
    {:classes classes-struct
     :typedefs (:typedefs struct)
     :enums enums-struct
     :namespaces namespaces-struct}))

(defn build [entries]
  (-> (group-by-kind entries)
      structurize))
