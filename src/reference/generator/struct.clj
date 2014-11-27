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

(defn build [entries]
  (-> (group-by-kind entries)))
