(ns reference.adoc.structs
  (:require [taoensso.timbre :as timbre :refer [info]]))

(defn- parse-description [description]
  description)

(defn- cleanup-name [name]
  (if name
    (clojure.string/replace (str name) #"['\"]" "")
    nil))

(defn- get-doclets-by-kind [doclets kind]
  (filter #(= kind (:kind %)) doclets))

(defn- get-doclets-by-memberof-and-kind [doclets memberof kind]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %))))))

(defn- get-doclets-with-filter [doclets memberof kind filter-fn]
  (let [longname (:longname memberof)] 
    (filter #(and (= kind (:kind %))
                  (= longname (:memberof %))
                  (filter-fn %)))))

(defn- is-static [entry]
  (= (:scope entry) "static"))

(defn- get-tag [doclet tag]
  (filter #(= (:title %) tag) (:tags doclet)))

(defn- parse-general [entry]
  {:name (:name entry)
   :description (parse-description (:description entry))
   :full-name (cleanup-name (:longname entry))
   :since (:since entry)})

(defn- parse-examples-and-listing [entry doclet]
  (let [samples (:examples doclet)
        listings (get-tag doclet "listing")]
    (assoc entry
           :samples samples
           :has-samples (boolean (seq samples))
           :listings (map :value listings)
           :has-listings (boolean (seq (listings))))))

(defn- group-functions [functions]
  (reduce (fn [res val]
            (let [name (:name val)
                  group (get res name)]
              (assoc res name (if group
                                (conj group val)
                                [val]))) {} functions)))

(defn- create-typedef-property [prop]
  (assoc (parse-general prop)
         :type (get-in prop [:type :names])))

(defn- create-typedef [typedef doclets]
  (assoc (parse-examples-and-listing (parse-general typedef) typedef)
         :properties (map create-typedef-property (:properties typedef))
         :type (get-in prop [:type :names])))

(defn- create-enum-field [doclet]
  (assoc (parse-general doclet)
         :default (:defaultvalue doclet)))

(defn- get-enum-fields [enum doclets]
  (map create-enum-field (get-doclets-with-filter enum "member" is-static)))

(defn- create-enum [enum doclets]
  (assoc (parse-examples-and-listing (parse-general enum) enum)
         :fields (get-enum-fields enum doclets)))

(defn- create-constant [const doclets]
  (parse-examples-and-listing (parse-general const) const))

(defn- parse-function-return [ret]
  {:types (get-in ret [:type :names])
   :description (:description ret)})

(defn- parse-function-param [param]
  (assoc (parse-function-return param)
         :name (:name param)))

(defn- create-function [func doclets]
  (assoc (parse-examples-and-listing (parse-general func) func)
         :detailed-desc (:value (first (get-tag func "detailed")))
         :params (map parse-function-param (:params func))
         :returns (map parse-function-return (:returns func))))

;; class
;; - class
;; - enum
;; - function (non-static!)
(defn- create-class [class doclets]
  (assoc (parse-general class)
         :extends (:augments class)
         :has-extends (boolean (seq (:aumgnets class)))
         :enums (map #(create-enum % doclets)
                     (get-doclets-with-filter doclets
                                              class
                                              "member"
                                              #(:isEnum %)))
         :classes (map #(create-class % doclets)
                       (get-doclets-by-memberof-and-kind doclets
                                                         namespace
                                                         "class"))
         :functions (group-functions (map #(create-function % doclets)
                                          (get-doclets-with-filter doclets
                                                                   namespace
                                                                   "function"
                                                                   #(not (is-static %)))))))

;; namespace:
;; - namespace
;; - typedef
;; - enum
;; - class
;; - const
;; - static function
(defn- create-namespace [namespace doclets]
  {:info (parse-general namespace)
   :typedefs (map #(create-typedef % doclets)
                  (get-doclets-by-memberof-and-kind doclets
                                                    namespace
                                                    "typedef"))
   :enums (map #(create-enum % doclets)
               (get-doclets-with-filter doclets
                                        namespace
                                        "member"
                                        #(:isEnum %)))
   :classes (map #(create-class % doclets)
                 (get-doclets-by-memberof-and-kind doclets
                                                   namespace
                                                   "class"))
   :constants (map #(create-constant % doclets)
                   (get-doclets-with-filter doclets
                                            namespace
                                            "member"
                                            is-static))
   :functions (map #(create-function % doclets)
                   (get-doclets-with-filter doclets
                                            namespace
                                            "function"
                                            is-static))})

(defn structurize [doclets]
  (info "structurize")
  (let [namespaces (get-doclets-by-kind doclets "namespace")]
    ))
