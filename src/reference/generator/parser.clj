(ns reference.generator.parser
  (:require [reference.generator.parser.tags :as tags]
            [clojure.string :refer [trim]]))

(defn- parse-description [description]
  description)

(defn- cleanup-name [name]
  (if name
    (clojure.string/replace (str name) #"['\"]" "")
    nil))

(defn- static? [raw]
  (= (:scope raw) "static"))

(defn- scope-instance? [raw]
  (= (:scope raw) "instance"))

(defn- parse-examples [examples])

(defn- parse-general [raw]
  (let [samples (parse-examples (:examples raw))]
    {:name (:name raw)
     :kind (:kind raw)
     :scope (:scope raw)
     :description (parse-description (:description raw))
     :full-name (cleanup-name (:longname raw))
     :member-of (cleanup-name (:memberof raw))
     :examples samples
     :has-examples (seq samples)
     :illustrations (tags/get-tag raw "illustration")}))

(defn- parse-ns-def [raw] (parse-general raw))

(defn- parse-constant-def [raw]
  (let [constant-tag (first (tags/get-tag raw "define"))
        parsed-constant-tag (re-matches #"\{(.*)\}(.*)" constant-tag)]
    (assoc (parse-general raw)
      :kind "constant"
      :value (get-in raw [:meta :code :value])
      :type (trim (nth parsed-constant-tag 1))
      :short-description (parse-description (trim (nth parsed-constant-tag 2))))))

(defn- parse-field-def [raw]
  (assoc (parse-general raw)
    :type (get-in raw [:type :names])
    :static (static? raw)
    :kind "field"))

(defn- parse-enum-def [raw]
  (if (= (get-in raw [:meta :code :type]) "MemberExpression")
    (assoc (parse-general raw)
      :kind "enum"
      :linked true
      :linked-to (get-in raw [:meta :code :value]))
    (let [fields (map (fn [f]
                        {:name (:name f)
                         :description (parse-description (:description f))
                         :has-description (not (clojure.string/blank? (:description f)))
                         :value (:defaultvalue f)}) (:properties raw))]
      (assoc (parse-general raw)
        :kind "enum"
        :type (get-in raw [:type :names])
        :has-type (seq (get-in raw [:type :names]))
        :fields fields
        :has-fields (seq fields)))))

(defn- parse-member-def [raw]
  (if (and (tags/contains-tag? raw "define")
           (static? raw))
    (parse-constant-def raw)
    (if (and (> (count (get-in raw [:type :names])) 0)
             (not (:isEnum raw)))
      (parse-field-def raw)
      (if (and (:isEnum raw) (static? raw))
        (parse-enum-def raw)))))

(defn- parse-function-return [raw]
  {:description (parse-description (:description raw))
   :optional (:optional raw)
   :default (:defaultvalue raw)
   :type (get-in raw [:type :names])})

(defn- parse-function-def [raw]
  (let [returns (map parse-function-return (:returns raw))
        params (map #(assoc (parse-function-return %)
                       :name (:name %))
                    (:params raw))]
    (assoc (parse-general raw)
      :inherit-doc (tags/inherit-doc? raw)
      :params params
      :has-params (seq params)
      :returns returns
      :has-returns (seq returns)
      :params-signature (clojure.string/join ", " (get-in raw [:meta :code :parameters])))))

(defn- parse-class-def [raw]
  (assoc (parse-general raw)
    :inherits (:augments raw)
    :has-inherits (seq (:augments raw))
    :constructor (parse-function-def raw)))

(defn- parse-typedef-def [raw]
  (let [props (map #({:name (:name %)
                      :description (parse-description (:description %))
                      :type (get-in % [:type :names])}) (:properties raw))]
    (assoc (parse-general raw)
      :properties props
      :has-properties (seq? props)
      :type (get-in raw [:type :names]))))

(defn- parse-entry [raw]
  (case (:kind raw)
    "namespace" (parse-ns-def raw)
    "member" (parse-member-def raw)
    "class" (parse-class-def raw)
    "typedef" (parse-typedef-def raw)
    "function" (parse-function-def raw)
    nil))

(defn parse [jsdoc]
  (filter #(not (nil? %)) (map parse-entry jsdoc)))
