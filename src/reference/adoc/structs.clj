(ns reference.adoc.structs
  (:require [taoensso.timbre :as timbre :refer [info]]
            [reference.adoc.media :refer [update-links]]))

(defn- parse-description [description version]
  (update-links description version))

(defn- cleanup-name [name]
  (if name
    (clojure.string/replace (str name) #"['\"]" "")
    nil))

(defn- get-doclets-by-kind [doclets kind]
  (filter #(= kind (:kind %)) doclets))

(defn- get-doclets-by-memberof-and-kind [doclets memberof kind]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %)))
            doclets)))

(defn- is-static [entry]
  (= (:scope entry) "static"))

(defn- is-function [entry]
  (or (= (:kind entry) "function")
      (and (= (:kind entry) "member")
           (or (boolean (seq (:params entry)))
               (boolean (seq (:returns entry)))))))

(defn- get-doclets-with-filter [doclets memberof kind filter-fn]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %))
                  (filter-fn %))
            doclets)))

(defn- get-functions [doclets memberof]
  (let [longname (:longname memberof)] 
    (filter #(and (is-function %)
                  (= longname (:memberof %))
                  (not (is-static %)))
            doclets)))

(defn- get-static-functions [doclets memberof]
  (let [longname (:longname memberof)] 
    (filter #(and (is-function %)
                  (= longname (:memberof %))
                  (is-static %))
            doclets)))

(defn- get-members [doclets memberof]
  (let [longname (:longname memberof)] 
    (filter #(and (not (is-function %))
                  (= (:kind %) "member")
                  (= longname (:memberof %))
                  (not (is-static %)))
            doclets)))

(defn- get-static-members [doclets memberof]
  (let [longname (:longname memberof)] 
    (filter #(and (not (is-function %))
                  (= (:kind %) "member")
                  (= longname (:memberof %))
                  (is-static %)
                  (not (:isEnum %)))
            doclets)))

(defn- get-enums [doclets memberof]
  (let [longname (:longname memberof)] 
    (filter #(and (not (is-function %))
                  (= (:kind %) "member")
                  (= longname (:memberof %))
                  (:isEnum %)
                  (is-static %))
            doclets)))

(defn- get-tag [doclet tag]
  (filter #(= (:title %) tag) (:tags doclet)))

(defn- parse-general [entry version]
  {:name (:name entry)
   :description (parse-description (:description entry) version)
   :has-description (not (empty? (:description entry)))
   :full-name (cleanup-name (:longname entry))
   :since (:since entry)})

(defn- parse-example [example]
  (if (re-find #" " (clojure.string/trim example))
    (let [[file title] (rest (re-matches #"^([^ ]+)( .*)" (clojure.string/trim example)))]
      {:file file
       :title (if-not (empty? title)
                (clojure.string/trim title)
                file)})
    {:file example
     :title example}))

(defn- parse-examples-and-listing [entry doclet]
  (let [samples (map parse-example (:examples doclet))
        listings (get-tag doclet "listing")]
    (assoc entry
           :playground-samples samples
           :has-playground-samples (boolean (seq samples))
           :listings (map :value listings)
           :has-listings (boolean (seq listings)))))

(defn- group-functions [functions]
  (if-not (empty? functions)
    (map (fn [[name methods]]
           {:name name
            :overrides methods})
         (reduce (fn [res val]
                   (let [name (:name val)
                         group (get res name)
                         group-val (if group
                                     (conj group val)
                                     [val])]
                     (assoc res name group-val)))
                 {} functions))
  []))

(defn- create-typedef-property [prop version]
  (assoc (parse-general prop version)
         :type (get-in prop [:type :names])))

(defn- create-typedef [typedef doclets version]
  (assoc (parse-examples-and-listing (parse-general typedef version) typedef)
         :kind :typedef
         :properties (map #(create-typedef-property % version) (:properties typedef))
         :has-properties (not (empty? (:properties typedef)))
         :type (get-in typedef [:type :names])
         :has-types (> (count (get-in typedef [:type :names])) 1)))

(defn- create-enum-field [doclet version]
  (assoc (parse-general doclet version)
         :value (:defaultvalue doclet)))

(defn- get-enum-fields [enum doclets version]
  (map #(create-enum-field % version) (get-static-members doclets enum)))

(defn- create-enum [enum doclets version]
  (assoc (parse-examples-and-listing (parse-general enum version) enum)
         :kind :enum
         :linked (if (and (= (get-in enum [:meta :code :type]) "MemberExpression")
                          (get-in enum [:meta :code :value]))
                   (get-in enum [:meta :code :value])
                   false)
         :fields (get-enum-fields enum doclets version)))

(defn- check-enum-link [enum enums]
  (if (:linked enum)
    (if-let [linked (first (filter #(= (:full-name %) (:linked enum))
                                   enums))]
      (assoc enum :fields (:fields (check-enum-link linked enums)))
      enum)
    enum))

(defn- create-constant [const doclets version]
  (parse-examples-and-listing (parse-general const version) const))

(defn- parse-function-return [ret version]
  {:types (get-in ret [:type :names])
   :description (parse-description (:description ret) version)})

(defn- parse-function-param [param version]
  (assoc (parse-function-return param version)
         :name (clojure.string/replace (:name param) #"^opt_" "")))

(defn- create-function-signature [name params]
  (str name "(" (clojure.string/join ", " (map :name params)) ")"))

(defn- create-function [func doclets version]
  (let [params (map parse-function-param (:params func) version)]
    (assoc (parse-examples-and-listing (parse-general func version) func)
           :kind :function
           :has-detailed (boolean (:value (first (get-tag func "detailed"))))
           :detailed (:value (first (get-tag func "detailed")))
           :has-params (boolean (seq params))
           :params params
           :signature (create-function-signature (:name func) params)
           :has-returns (boolean (seq (:returns func)))
           :returns (map parse-function-return (:returns func) version))))

;; class
;; - class
;; - enum
;; - function (non-static!)
(defn- create-class [class doclets version]
  (assoc (parse-general class version)
         :kind :class
         :extends (:augments class)
         :has-extends (boolean (seq (:augments class)))
         :enums (map :longname (get-enums doclets class))
         :classes (map :longname
                       (get-doclets-by-memberof-and-kind doclets
                                                         namespace
                                                         "class"))
         :methods (group-functions
                   (map #(create-function % doclets version)
                        (get-functions doclets class)))))

;; namespace:
;; - namespace
;; - typedef
;; - enum
;; - class
;; - const
;; - static function
(defn- create-namespace [namespace doclets version]
  (assoc (parse-general namespace version)
         :parent (:memberof namespace)
         :typedefs (map :longname (get-doclets-by-memberof-and-kind doclets
                                                                    namespace
                                                                    "typedef"))
         :enums (map :longname (get-enums doclets namespace))
         :classes (map :longname
                       (get-doclets-by-memberof-and-kind doclets
                                                         namespace
                                                         "class"))
         :constants (map #(create-constant % doclets version)
                         (get-static-members doclets namespace))
         :functions (map #(create-function % doclets version)
                         (get-static-functions doclets namespace))))

(defn structurize [doclets version]
  (info "structurize doclets")
  {:classes (map #(create-class % doclets version) (get-doclets-by-kind doclets "class"))
   :namespaces (map #(create-namespace % doclets version)
                    (get-doclets-by-kind doclets "namespace"))
   :typedefs (map #(create-typedef % doclets version)
                  (get-doclets-by-kind doclets "typedef"))
   :enums (let [enums (map #(create-enum % doclets version)
                           (filter #(and (= "member" (:kind %))
                                         (:isEnum %)) doclets))]
            (map #(check-enum-link % enums) enums))})
