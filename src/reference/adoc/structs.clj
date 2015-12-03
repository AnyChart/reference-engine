(ns reference.adoc.structs
  (:require [taoensso.timbre :as timbre :refer [info]]
            [reference.adoc.media :refer [update-links]]
            [reference.git :refer [file-last-commit-date]]
            [clojure.string :refer [blank?]]))

(def id-counter (atom 0))

(defn- get-relative-path [doclet base-path]
  (let [fname (-> doclet :meta :filename)
        path (-> doclet :meta :path)
        full-path (str path "/" fname)]
    (if full-path
      (clojure.string/replace
       (clojure.string/replace full-path
                               (clojure.string/re-quote-replacement base-path)
                               "")
       #"adoc\.js" "adoc"))))

(defn- get-last-modified [cache path base-path]
  (if (contains? @cache path)
    (get @cache path)
    (let [last-modified (file-last-commit-date base-path path)]
      (swap! cache assoc path last-modified)
      last-modified)))

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

(defn- get-doclet-by-fullname-and-kind [doclets fullname kind]
  (first (filter #(and (= kind (:kind %))
                       (= fullname (:longname %)))
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

(defn- get-short-description [entry version]
  (parse-description
   (if (seq (get-tag entry "shortdescription"))
     (:text (first (get-tag entry "shortdescription")))
     (if (:description entry)
       (if (.contains (:description entry) "\n")
         (subs (:description entry) 0 (.indexOf (:description entry) "\n"))
         (:description entry))
       ""))
   version))

(defn- has-short-description [entry]
  (boolean (seq (get-tag entry "shortdescription"))))

(defn- parse-general [entry version]
  {:name (:name entry)
   :description (parse-description (:description entry) version)
   :short-description (get-short-description entry version)
   :has-short-description (has-short-description entry)
   :has-description (not (blank? (:description entry)))
   :full-name (cleanup-name (:longname entry))
   :since (:since entry)
   :has-since (blank? (:since entry))})

(defn- get-example-link [base-path doclet file]
  (let [folder (clojure.string/replace (get-in doclet [:meta :path])
                               (clojure.string/re-quote-replacement base-path)
                               "")]
    (if folder
      (str folder "/" file)
      file)))

(defn- parse-example [base-path doclet example]
  (if (re-find #" " (clojure.string/trim example))
    (let [[file title] (rest (re-matches #"^([^ ]+)( .*)" (clojure.string/trim example)))]
      {:file (get-example-link base-path doclet file)
       :title (if-not (empty? title)
                (clojure.string/trim title)
                "Usage sample")})
    {:file (get-example-link base-path doclet example)
     :title "Usage sample"}))

(defn- parse-examples [base-path doclet examples]
  (map #(parse-example base-path doclet %) examples))

(defn- parse-listing [listing]
  (let [title (last (re-find #"(?s)^([^\n]*)\n" listing))]
    {:id (swap! id-counter inc)
     :title (if (blank? title)
              "Example"
              title)
     :code (last (re-find #"(?s)^[^\n]*\n(.*)" listing))}))

(defn- parse-examples-and-listing [base-path entry doclet]
  (let [samples (parse-examples base-path doclet (:examples doclet))
        listings (map parse-listing (map :value (get-tag doclet "listing")))]
    (assoc entry
           :playground-samples samples
           :has-playground-samples (boolean (seq samples))
           :listings listings
           :has-listings (boolean (seq listings)))))

(defn- get-functions-group-short-description [methods]
  (if-let [method (first (filter #(:has-short-description %) methods))]
    (:short-description method)
    (:short-description (first methods))))

(defn- group-functions [functions]
  (if-not (empty? functions)
    (sort-by
     :name
     (map (fn [[name methods]]
            {:name name
             :overrides methods
             :short-description (get-functions-group-short-description methods)})
          (reduce (fn [res val]
                    (let [name (:name val)
                          group (get res name)
                          group-val (if group
                                      (conj group val)
                                      [val])]
                      (assoc res name group-val)))
                  {} functions)))
  []))

(defn- create-typedef-property [prop version]
  (assoc (parse-general prop version)
         :type (get-in prop [:type :names])))

(defn- create-typedef [typedef doclets version base-path last-mod-cache]
  (let [path (get-relative-path typedef base-path)]
    (assoc (parse-examples-and-listing base-path (parse-general typedef version) typedef)
           :file path
           :last-modified (get-last-modified last-mod-cache path base-path)
           :kind :typedef
           :properties (sort-by :name
                                (map #(create-typedef-property % version)
                                     (:properties typedef)))
           :has-properties (not (empty? (:properties typedef)))
           :type (get-in typedef [:type :names])
           :has-types (> (count (get-in typedef [:type :names])) 1))))

(defn- create-enum-field [doclet version]
  (assoc (parse-general doclet version)
         :value (get-in doclet [:meta :code :value])))

(defn- get-enum-fields [enum doclets version]
  (sort-by :name
           (map #(create-enum-field % version) (get-static-members doclets enum))))

(defn- create-enum [enum doclets version base-path last-mod-cache]
  (let [path (get-relative-path enum base-path)]
    (assoc (parse-examples-and-listing base-path (parse-general enum version) enum)
           :file path
           :last-modified (get-last-modified last-mod-cache path base-path)
           :kind :enum
           :linked (if (and (= (get-in enum [:meta :code :type]) "MemberExpression")
                            (get-in enum [:meta :code :value]))
                     (get-in enum [:meta :code :value])
                     false)
           :fields (get-enum-fields enum doclets version))))

(defn- check-enum-link [enum enums]
  (if (:linked enum)
    (if-let [linked (first (filter #(= (:full-name %) (:linked enum))
                                   enums))]
      (assoc enum :fields (:fields (check-enum-link linked enums)))
      enum)
    enum))

(defn- create-constant [const doclets version base-path]
  (parse-examples-and-listing base-path (parse-general const version) const))

(defn- parse-function-return [ret version]
  {:types (get-in ret [:type :names])
   :description (parse-description (:description ret) version)})

(defn- parse-param-description [description version]
  (if description
    (clojure.string/replace (parse-description description version)
                            #"\s*\[[^\]]+\]\s*" "")))

(defn- convert-code-to-list [code]
  (let [lines (clojure.string/split-lines code)]
    (if (= (count lines) 1)
      code
      (str "<ul>" (reduce str (map #(str "<li>" % "</li>") lines)) "</ul>")))
  code)

(defn- get-param-default [param]
  (if (and (:description param)
           ;;(re-find #"^opt_" (:name param))
           (re-find #"\s*\[[^\]]+\]\s*" (:description param)))
    (last (re-find #"^\[([^\]]+)\]" (:description param)))))

(defn- parse-param-default [param]
  (get-param-default param))

(defn- parse-function-param [param version]
  {:types (get-in param [:type :names])
   :description (parse-param-description (:description param) version)
   :name (if (:name param)
           (clojure.string/replace (:name param) #"^opt_" ""))
   :default (parse-param-default param)})

(defn- function-has-params-defaults [params]
  (boolean (some #(:default %) params)))

(defn- create-function-signature [name params]
  (str name "(" (clojure.string/join ", " (map :name params)) ")"))

(defn- create-function [func doclets version base-path]
  (let [params (map parse-function-param (:params func) version)
        returns (map parse-function-return (:returns func) version)]
    (assoc (parse-examples-and-listing base-path (parse-general func version) func)
           :kind :function
           :has-detailed (boolean (:value (first (get-tag func "detailed"))))
           :category (:value (first (get-tag func "category")))
           :has-category (boolean (:value (first (get-tag func "category"))))
           :detailed (parse-description (:value (first (get-tag func "detailed"))) version) 
           :has-params (boolean (seq params))
           :has-params-defaults (function-has-params-defaults params)
           :params params
           :signature (create-function-signature (:name func) params)
           :has-returns (boolean (seq returns))
           :returns returns)))

(defn- create-link-struct [entry version]
  {:name (:longname entry)
   :short-description (get-short-description entry version)})

;; class
;; - enum
;; - function (non-static!)
(defn- create-class [class doclets version base-path last-mod-cache]
  (let [parent (get-doclet-by-fullname-and-kind doclets (:memberof class) "class")
        path (get-relative-path class base-path)]
    (assoc (parse-general class version)
           :file path
           :last-modified (get-last-modified last-mod-cache path base-path)
           :name (if parent
                   (str (:name parent) "." (:name class))
                   (:name class))
           :parent (:memberof class)
           :kind :class
           :extends (:augments class)
           :has-extends (boolean (seq (:augments class)))
           :enums (sort-by :name (map #(create-link-struct % version)
                                      (get-enums doclets class)))
           :methods (group-functions
                     (map #(create-function % doclets version base-path)
                          (get-functions doclets class))))))

;; namespace:
;; - namespace
;; - typedef
;; - enum
;; - class
;; - const
;; - static function
(defn- create-namespace [namespace doclets version base-path last-mod-cache]
  (let [path (get-relative-path namespace base-path)]
    (assoc (parse-general namespace version)
           :file path
           :last-modified (get-last-modified last-mod-cache path base-path)
           :parent (:memberof namespace)
           :typedefs (sort-by :name (map #(create-link-struct % version)
                                         (get-doclets-by-memberof-and-kind doclets
                                                                           namespace
                                                                           "typedef")))
           :enums (sort-by :name (map #(create-link-struct % version)
                                      (get-enums doclets namespace)))
           :classes (sort-by :name
                             (map #(create-link-struct % version)
                                  (apply concat
                                         (map #(concat [%]
                                                       (get-doclets-by-memberof-and-kind
                                                        doclets
                                                        %
                                                        "class"))
                                              (get-doclets-by-memberof-and-kind doclets
                                                                                namespace
                                                                                "class")))))
           :constants (sort-by :name
                               (map #(create-constant % doclets version base-path)
                                    (get-static-members doclets namespace)))
           :functions (sort-by :name
                               (map #(create-function % doclets version base-path)
                                    (get-static-functions doclets namespace))))))

(defn- get-unique-namespaces [doclets]
  (let [namespaces (get-doclets-by-kind doclets "namespace")]
    (reduce (fn [res ns]
              (if (some #(= (:longname %)
                            (:longname ns))
                        res)
                res
                (conj res ns)))
            [] namespaces)))

(defn- fix-class-names [class classes])

(defn structurize [doclets data-path version]
  (info "structurize doclets")
  (let [base-path (str data-path "/versions-tmp/" version)
        last-mod-cache (atom {})]
    {:classes (map #(create-class % doclets version base-path last-mod-cache)
                   (get-doclets-by-kind doclets "class"))
     :namespaces (map #(create-namespace % doclets version base-path last-mod-cache)
                      (get-unique-namespaces doclets))
     :typedefs (map #(create-typedef % doclets version base-path last-mod-cache)
                    (get-doclets-by-kind doclets "typedef"))
     :enums (let [enums (map #(create-enum % doclets version base-path last-mod-cache)
                             (filter #(and (= "member" (:kind %))
                                           (:isEnum %)) doclets))]
              (map #(check-enum-link % enums) enums))}))
