(ns reference.generator.struct)

(defn- add-member [struct entry]
  (let [member-of (:member-of entry)]
    (if (contains? struct member-of)
      (assoc-in struct [member-of :members] (conj (get-in struct [member-of :members]) entry))
      (assoc struct member-of {:name member-of :entries [] :members [entry]}))))

(defn- add-entry [struct entry]
  (let [name (:full-name entry)]
    (if (contains? struct name)
      (assoc-in struct [name :entries] (conj (get-in struct [name :entries] entry)))
      (assoc struct name {:name name :entries [entry] :members []}))))

(defn- add-entry-to-struct
  ([] {})
  ([res entry]
     (add-member (add-entry res entry) entry)))

(defn build [entries]
  (reduce add-entry-to-struct {} entries))
