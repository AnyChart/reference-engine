(ns reference-engine.parser)

(defn filter-jsdoc-info [info]
  (filter (fn [info] (not (= (:access info) "private")))
          info))

(defn ignore-doc? [raw]
  (some #(= (:originalTitle %) "ignoreDoc") (:tags raw)))

(defn inherit-doc? [raw]
  (some #(= (:originalTitle %) "inheritDoc") (:tags raw)))

(defn filter-raw [raw]
  (filter (fn [info] (not (or (= (:access info) "private")
                              (= (:access info) "protected")
                              (= (:scope info) "inner")
                              (ignore-doc? info)))) raw))

(defn ignore-circular [obj]
  (if (= obj "<CircularRef>")
    nil
    obj))

(defn parse-general-member [member]
  {:name (:name member)
   :static? (= (:scope member) "static")
   :access (:access member)
   :inherit-doc? (inherit-doc? member)
   :examples (:examples member)
   :kind (:kind member)
   :illustrations (map :text (filter #(= (:originalTitle %) "illustration")
                                     (:tags member)))})

(defn parse-typed-member [member]
  (assoc (parse-general-member member)
    :type (get-in member [:type :names])))

(defn parse-param [param]
  {:description (:description param)
   :optional? (:optional param)
   :name (:name param)
   :type (get-in param [:type :names])})

(defn parse-return [return]
  (let [info (first return)]
    {:description (:description info)
     :type (get-in info [:type :names])}))

(defn parse-function-member [member]
  (assoc (parse-general-member member)
    :returns (parse-return (:returns member))
    :params (map parse-param (:params member))))

(defn group-members [members]
  (let [names (set (map :name members))]
    (map
     (fn [name]
       {:name name
        :members (filter (fn [member] (= (:name member) name)) members)})
     names)))

(defn parse-member [member]
  (case (:kind member)
    "function" (parse-function-member member)
    "member" (parse-typed-member member)
    (parse-general-member member)))

(defn get-members [data class]
  (sort-by
   :name
   (group-members
    (map parse-member
         (filter #(= (:memberof %) (:full-name class))
                 data)))))

(defn get-methods [data class]
  (filter
   (fn [member]
     (= (:kind (first (:members member))) "function"))
   (get-members data class)))

(defn get-props [data class]
  (filter
   (fn [member]
     (= (:kind (first (:members member))) "member"))
   (get-members data class)))

(defn get-classes [data]
  (map (fn [meta] (assoc (parse-general-member meta)
                    :full-name (:longname meta)
                    :package (:memberof meta)
                    :extends (:augments meta)))
       (filter #(= (:kind %) "class") data)))

(defn parse-jsdoc [raw]
  (let [data (filter-raw raw)]
    (map (fn [class]
           (assoc class
             :methods (get-methods data class)
             :properties (get-props data class)))
         (get-classes data))))
