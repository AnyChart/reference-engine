(ns reference.generator.parser
  (:require [reference.generator.parser.tags :as tags]
            [reference.generator.samples :refer [parse-sample]]
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

(defn- parse-examples [entry-name examples version]
  (if (and (> (count examples) 0)
           (not (= examples "<CircularRef>")))
    (map #(parse-sample entry-name % version) examples)))

(defn- parse-general [raw version]
  (let [samples (parse-examples (cleanup-name (:longname raw))
                                (:examples raw)
                                version)]
    {:name (:name raw)
     :kind (:kind raw)
     :scope (:scope raw)
     :static (static? raw)
     :force-include (tags/contains-tag? raw "includeDoc")
     :description (parse-description (:description raw))
     :full-name (cleanup-name (:longname raw))
     :member-of (cleanup-name (:memberof raw))
     :examples samples
     :has-examples (boolean (seq samples))
     :illustrations (tags/get-tag raw "illustration")}))

(defn- parse-ns-def [raw version] (parse-general raw version))

(defn- parse-constant-def [raw version]
  (let [constant-tag (first (tags/get-tag raw "define"))
        parsed-constant-tag (re-matches #"\{(.*)\}(.*)" constant-tag)]
    (assoc (parse-general raw version)
      :kind "constant"
      :value (get-in raw [:meta :code :value])
      :type (trim (nth parsed-constant-tag 1))
      :short-description (parse-description (trim (nth parsed-constant-tag 2))))))

(defn- parse-field-def [raw version]
  (assoc (parse-general raw version)
    :type (get-in raw [:type :names])
    :static (static? raw)
    :kind "field"))

(defn- parse-enum-def [raw version]
  (if (= (get-in raw [:meta :code :type]) "MemberExpression")
    (assoc (parse-general raw version)
      :kind "enum"
      :linked true
      :linked-to (get-in raw [:meta :code :value]))
    (let [fields (map (fn [f]
                        {:name (:name f)
                         :description (parse-description (:description f))
                         :has-description (not (clojure.string/blank? (:description f)))
                         :value (:defaultvalue f)}) (:properties raw))]
      (assoc (parse-general raw version)
        :kind "enum"
        :type (get-in raw [:type :names])
        :has-type (boolean (seq (get-in raw [:type :names])))
        :fields fields
        :has-fields (boolean (seq fields))))))

(defn- parse-member-def [raw version]
  (if (and (tags/contains-tag? raw "define")
           (static? raw))
    (parse-constant-def raw version)
    (if (and (> (count (get-in raw [:type :names])) 0)
             (not (:isEnum raw)))
      (parse-field-def raw version)
      (if (and (:isEnum raw) (static? raw))
        (parse-enum-def raw version)))))

(defn- parse-function-return [raw]
  {:description (parse-description (:description raw))
   :optional (:optional raw)
   :default (:defaultvalue raw)
   :type (get-in raw [:type :names])})

(defn- parse-function-def [raw version]
  (let [returns (map parse-function-return (:returns raw))
        params (map #(assoc (parse-function-return %)
                       :name (:name %))
                    (:params raw))]
    (assoc (parse-general raw version)
      :inherit-doc (tags/inherit-doc? raw)
      :params params
      :has-params (boolean (seq params))
      :returns returns
      :has-returns (boolean (seq returns))
      :params-signature (clojure.string/join ", " (get-in raw [:meta :code :parameters])))))

(defn- parse-class-def [raw version]
  (assoc (parse-general raw version)
    :inherits (:augments raw)
    :has-inherits (boolean (seq (:augments raw)))
    :constructor (parse-function-def raw version)))

(defn- parse-typedef-def [raw version]
  (let [props (map (fn [prop]
                     {:name (:name prop)
                      :description (parse-description (:description prop))
                      :type (get-in prop [:type :names])})
                   (:properties raw))]
    (assoc (parse-general raw version)
      :properties props
      :has-properties (boolean (seq props))
      :type (get-in raw [:type :names]))))

(defn- parse-entry [raw version]
  (if-not (tags/contains-tag? raw "ignoreDoc")
    (case (:kind raw)
      "namespace" (parse-ns-def raw version)
      "member" (parse-member-def raw version)
      "class" (parse-class-def raw version)
      "typedef" (parse-typedef-def raw version)
      "function" (parse-function-def raw version)
      nil)))

(defn parse [jsdoc version]
  (filter #(not (nil? %)) (map #(parse-entry % version) jsdoc)))
