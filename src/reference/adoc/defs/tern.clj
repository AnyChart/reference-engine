(ns reference.adoc.defs.tern
  (:require [clojure.string :as s :refer [join]]
            [taoensso.timbre :as timbre :refer [info error]]
            [cheshire.core :refer [generate-string]]))

;(defonce ^:const t2 "  ")
;(defonce ^:const tab4 "    ")
;(defonce ^:const tab6 "      ")
;(defonce ^:const tab8 "        ")

;; for developing
(defonce top-level (atom nil))
(defonce tree (atom nil))

(defn set-top-level! [_top-level _tree]
  (reset! top-level _top-level)
  (reset! tree _tree))

;(defn shift [tab]
;  (str tab t2))
;;================
(defn get-ns [full-name top-level]
  (first (filter #(= full-name (:full-name %)) (:namespaces top-level))))

;;;======================== printing
;(defn print-constant [constant tab]
;  (str tab "\"" (:name constant) "\": {"
;      (when (:type constant) (str "\"!type\": \"" (:type constant) "\", "))
;       "\"!doc\": \"" (or (:short-description constant) (:description constant)) "\""
;       "}"))
;
;(defn build-ns [ns top-level tab]
;  (let [full-ns (get-ns (:full-name ns) top-level)
;        nss (filter #(= (:kind %) :namespace) (:children ns))]
;    (str tab "\"" (:name ns) "\": {\n"
;         (join ",\n" (map #(print-constant % (shift tab)) (:constants full-ns)))
;         (when (and (seq (:constants full-ns)) (seq nss)) ",")
;         (when (seq (:constants full-ns)) "\n")
;
;
;         (join ",\n" (map #(build-ns % top-level (shift tab)) nss))
;         (when (seq nss) "\n")
;
;         tab "}"))
;  )
;
;(defn build [ns top-level]
;  (str "{\n"
;       t2 "\"!name\": \"anychart\",\n"
;       (build-ns ns top-level t2)
;       "\n}"
;       ))

;;====================================== object
(defn get-enums [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:enums top-level)))

(defn get-classes [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:classes top-level)))

(defn get-typedefs [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:typedefs top-level)))

(defn remove-tags [html]
  (clojure.string/replace html #"<[^>]*>" ""))

(defn description [object]
  (-> (or (:short-description object) (:description object))
      remove-tags))

(defn url [object]
  (str "https://api.anychart.com/7.11.1/" (:full-name object)))

(defn parse-object-type [t]
  (if (.startsWith t "Object.<")
    "+Object"
    t))

(defn get-type [t]
  (let [t (parse-object-type (clojure.string/replace t #"\|undefined" ""))]
    (when
      (and (.contains t "Object<") (.contains t "Array<"))
      (throw "Too complicated type"))
    (case t
      "function" "fn()"
      "Array" "+Array"
      (-> t
          (s/replace #"scope" "+Object")
          (s/replace #"\*" "+Object")
          (s/replace #"anychart" "+anychart")
          (s/replace #"boolean" "bool")
          ;(s/replace #"!?Array<([^>]*)>" "[$1]")
          (clojure.string/replace #"!?Array.\<\(?" "[")
          (clojure.string/replace #"\)?>" "]")
          (s/replace #"!" "")
          (s/replace #"\|null" "")))))

(defn get-types [types]
  (join "|" (map get-type
                   (filter (partial #(and (not= % "null") (not= % "undefined"))) types))))

(defn set-params [params arr]
  (let [rest-params (map rest params)
        first-params (map first params)]
    (cond
      (every? nil? first-params) arr
      (every? empty? rest-params) (conj arr (filter some? first-params))
      :else (set-params rest-params (conj arr (filter some? first-params))))))

(defn simplify-params [params]
  (map (fn [ps] {:types    (distinct (mapcat :types ps))
                 :name     (let [names (distinct (map :name ps))]
                             (if (> (count names) 1)
                               "value"
                               (first names)))
                 :optional (some some? (map :optional ps))}) params))

(defn create-function-params-str [params]
  (join ", " (map #(str (when (:optional %) "opt_")
                        (:name %)
                        (when (:optional %) "?") ": " (get-types (:types %))) params)))

(defn function-return [function]
  (let [rets (distinct (mapcat :types (map first (map :returns (:overrides function)))))]
    (when (= "number" (:name function))
      (prn rets))
    (when (seq rets)
      (str " -> " (get-types rets)))))

(defn create-function-type [function]
  (let [params (map :params (:overrides function))
        grouped-params (set-params params [])
        simply-params (simplify-params grouped-params)]
    (when (= "number" (:name function))
      (prn params)
      (prn grouped-params)
      (prn simply-params))
    (str "fn("  (create-function-params-str simply-params)  ")" (function-return function))))

(defn create-function [function]
  {"!type" (create-function-type function)
   "!url"  (str "https://api.anychart.com/7.11.1/" (let [f (first (:overrides function))]
                                                     (clojure.string/replace (:full-name f)
                                                                             (re-pattern (str "." (:name f)))
                                                                             (str "#" (:name f)))))
   "!doc"  (description function)})

(defn create-constant [constant]
  (let [res {"!doc" (or (:short-description constant) (:description constant))}]
    (if (:type constant)
      (assoc res "!type" (:type constant))
      res)))

(defn create-enum-field [field parent]
  {"!doc" (description field)
   "!url" (url field)
   "!type" (str  parent) })

(defn create-enum [enum]
  (let [type (if (-> enum :fields first :value string?) "string" "number")
        result1 {"!doc" (str (description enum) " @enum {" type "}")
                 "!url" (url enum)}
        result2 (reduce #(assoc %1 (:name %2) (create-enum-field %2 (:full-name enum)))
                                     result1
                                     (:fields enum))]
  result2))

(defn create-typedef [typedef]
  ;(prn "Typedef: " typedef)
  (if (empty? (:properties typedef))
    {"!doc" (str (description typedef) " @typedef {(" (join "|" (:type typedef)) ")}")
     "!url" (url typedef)}
    {"!doc"      (description typedef)
     "!url"      (url typedef)
     "prototype" (reduce #(assoc %1 (:name %2) {"!type" (get-types (:type %2))
                                                "!doc" (description %2)})
                         {}
                         (:properties typedef))}))

(defn treefy-class [class inner-classes]
  (let [classes (filter #(.startsWith (:full-name %) (str (:full-name class) ".")) inner-classes)]
    (if (seq classes)
      (do
        (prn (:full-name class) (map :full-name classes))
        (assoc class :classes classes))
      class)))

(defn treefy-classes [classes]
  (let [inner-classes (filter #(.contains (:name %) ".") classes)
        outer-classes (filter #(not (.contains (:name %) ".")) classes)
        result (map (fn [class] (let [childred (filter #(= (:parent %) (:full-name class)) inner-classes)]
                                  (assoc class :classes
                                               (map #(assoc % :name
                                                              (last (clojure.string/split (:name %) #"\.")))
                                                    childred))))
                    outer-classes)]
    result))


(defn create-class [class top-level]
  (let [result {"!doc"      (description class)
                "!url"      (url class)
                "prototype" (reduce #(assoc %1 (:name %2) (create-function %2))
                                    {}
                                    (:methods class))}
        result-enums (reduce #(assoc %1 (:name %2) (create-enum %2))
                             result
                             (get-enums top-level (map :name (:enums class))))

        result-typedefs (reduce #(assoc %1 (:name %2) (create-typedef %2))
                                result-enums
                                (get-typedefs top-level (map :name (:typedefs class))))

        result-classes (reduce #(assoc %1 (:name %2) (create-class %2 top-level))
                               result-typedefs
                               (:classes class))]
    result-classes))


(defn build-ns [ns top-level]
  (let [full-ns (get-ns (:full-name ns) top-level)
        tree-namespaces (filter #(= (:kind %) :namespace) (:children ns))

        result-constants (reduce #(assoc %1 (:name %2) (create-constant %2)) {} (:constants full-ns))
        result-namespaces (reduce #(assoc %1 (:name %2) (build-ns %2 top-level)) result-constants tree-namespaces)

        result-functions (reduce #(assoc %1 (:name %2) (create-function %2)) result-namespaces (:functions full-ns))

        result-classes (reduce #(assoc %1 (:name %2) (create-class %2 top-level))
                               result-functions
                               (get-classes top-level (map :name (:classes full-ns))))

        result-enums (reduce #(assoc %1 (:name %2) (create-enum %2))
                             result-classes
                             (get-enums top-level (map :name (:enums full-ns))))

        result-typedefs (reduce #(assoc %1 (:name %2) (create-typedef %2))
                                result-enums
                               (get-typedefs top-level (map :name (:typedefs full-ns))))
        result result-typedefs]
    (assoc result "!doc" (description full-ns)
                  "!url" (url ns))))

(defn build [ns top-level]
  (assoc {} "!name" "anychart"
            "anychart" (build-ns ns
                                 (update top-level :classes treefy-classes ))))

(defn test2 []
  (let [result (build (first @tree) @top-level)
        json (generate-string result {:pretty true})]
    (spit "/media/ssd/sibental/reference-engine-data/tern/codeMirror/defs/anychart.json" json)))


(defn generate-declarations [data-dir version-key top-level]
  (info "generate TernJS definitions")
  (let [file-name (str data-dir "/versions-static/" version-key "/anychart.json")
        result (build (first @tree) @top-level)
        json (generate-string result {:pretty true})]
    (spit file-name json)))

