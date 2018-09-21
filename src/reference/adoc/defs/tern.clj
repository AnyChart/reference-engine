(ns reference.adoc.defs.tern
  (:require [taoensso.timbre :as timbre :refer [info error]]
            [cheshire.core :refer [generate-string]]
            [me.raynes.fs :as fs]
            [clojure.string :as string :refer [join]]))


;; for developing and testing
(defonce top-level (atom nil))
(defonce tree (atom nil))


(defn set-top-level! [_top-level _tree]
  (reset! top-level _top-level)
  (reset! tree _tree))


(defn get-ns [full-name top-level]
  (first (filter #(= full-name (:full-name %)) (:namespaces top-level))))


(defn get-enums [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:enums top-level)))


(defn get-classes [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:classes top-level)))


(defn get-typedefs [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:typedefs top-level)))


(defn remove-tags [html]
  (if (seq html) (string/replace html #"<[^>]*>" "") ""))


(defn description [object]
  (-> (or (:short-description object) (:description object))
      remove-tags))


(defn url [object settings]
  (str (:domain settings) (:version-key settings) "/" (:full-name object)))


(defn parse-object-type [t]
  (if (.startsWith t "Object.<")
    "+Object"
    t))


(defn get-type [t]
  (let [t (parse-object-type (string/replace t #"\|undefined" ""))]
    (when
      (and (.contains t "Object<") (.contains t "Array<"))
      (throw "Too complicated type"))
    (case t
      "function" "fn()"
      "Array" "+Array"
      (-> t
          (string/replace #"function" "fn")
          (string/replace #"scope" "+Object")
          (string/replace #"\*" "+Object")
          (string/replace #"anychart" "+anychart")
          (string/replace #"boolean" "bool")
          ;(string/replace #"!?Array<([^>]*)>" "[$1]")
          (string/replace #"!?Array.\<\(?" "[")
          (string/replace #"\)?>" "]")
          (string/replace #"!" "")
          (string/replace #"\|null" "")))))


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
    ;(when (= "number" (:name function))
    ;  (prn rets))
    (when (seq rets)
      (str " -> " (get-types rets)))))


(defn create-function-type [function]
  (let [params (map :params (:overrides function))
        grouped-params (set-params params [])
        simply-params (simplify-params grouped-params)]
    ;(when (= "number" (:name function))
    ;  (prn params)
    ;  (prn grouped-params)
    ;  (prn simply-params))
    (str "fn(" (create-function-params-str simply-params) ")" (function-return function))))


(defn create-function [function settings]
  {"!type" (create-function-type function)
   "!url"  (str (:domain settings) (:version-key settings) "/"
                (let [f (first (:overrides function))]
                  (string/replace (:full-name f)
                                  (re-pattern (str "." (:name f)))
                                  (str "#" (:name f)))))
   "!doc"  (description function)})


(defn create-constant [constant settings]
  (let [res {"!doc" (or (:short-description constant) (:description constant))
             "!url" (str (:domain settings) (:version-key settings) "/"
                         (string/replace (:full-name constant)
                                         (re-pattern (str "." (:name constant)))
                                         (str "#" (:name constant))))}]
    (if (:type constant)
      (assoc res "!type" (:type constant))
      res)))


(defn create-enum-field [field enum settings]
  {"!doc"  (description field)
   "!url"  (url enum settings)
   "!type" (:full-name enum)})


(defn create-enum [enum settings]
  (let [type (if (-> enum :fields first :value string?) "string" "number")
        result1 {"!doc" (str (description enum) " @enum {" type "}")
                 "!url" (url enum settings)}
        result2 (reduce #(assoc %1 (:name %2) (create-enum-field %2 enum settings))
                        result1
                        (:fields enum))]
    result2))


(defn create-typedef [typedef settings]
  (if (empty? (:properties typedef))
    {"!doc" (str (description typedef) " @typedef {(" (join "|" (:type typedef)) ")}")
     "!url" (url typedef settings)}
    {"!doc"      (description typedef)
     "!url"      (url typedef settings)
     "prototype" (reduce #(assoc %1 (:name %2) {"!type" (get-types (:type %2))
                                                "!doc"  (description %2)})
                         {}
                         (:properties typedef))}))


(defn treefy-class [class inner-classes]
  (let [classes (filter #(.startsWith (:full-name %) (str (:full-name class) ".")) inner-classes)]
    (if (seq classes)
      (do
        (prn (:full-name class) (map :full-name classes))
        (assoc class :classes classes))
      class)))


(defn treefy-classes
  "Divide classes on :
  Inner: Tree.DataItem TableSelectable.RowProxy TableComputer.RowProxy TreeView.DataItem Scroller.Thumb ...
  Outer: StockOrdinalDateTime ScatterTicks Linear .. and all majority of classes
  And add inner as children to outer classes"
  [classes]
  (let [inner-classes (filter #(.contains (:name %) ".") classes)
        outer-classes (filter #(not (.contains (:name %) ".")) classes)
        ;_ (println "Inner: " (map :name inner-classes))
        ;_ (println "Outer: " (map :name outer-classes))
        result (map (fn [class] (let [children (filter #(= (:parent %) (:full-name class)) inner-classes)]
                                  ;(when (seq children)
                                  ;  (println (str "Class:\n" class
                                  ;                "\nChildren:\n" (pr-str children)
                                  ;                "\n=====")))
                                  (assoc class :classes
                                               (map #(assoc % :name
                                                              (last (string/split (:name %) #"\.")))
                                                    children))))
                    outer-classes)]
    result))


(defn create-class [class top-level settings]
  (let [result {"!doc"      (description class)
                "!url"      (url class settings)
                "prototype" (reduce #(assoc %1 (:name %2) (create-function %2 settings))
                                    {}
                                    (:methods class))}
        result-enums (reduce #(assoc %1 (:name %2) (create-enum %2 settings))
                             result
                             (get-enums top-level (map :name (:enums class))))

        result-typedefs (reduce #(assoc %1 (:name %2) (create-typedef %2 settings))
                                result-enums
                                (get-typedefs top-level (map :name (:typedefs class))))

        result-classes (reduce #(assoc %1 (:name %2) (create-class %2 top-level settings))
                               result-typedefs
                               (:classes class))]
    result-classes))


(defn build-ns [ns top-level settings]
  (let [full-ns (get-ns (:full-name ns) top-level)
        tree-namespaces (filter #(= (:kind %) :namespace) (:children ns))

        result-constants (reduce #(assoc %1 (:name %2) (create-constant %2 settings)) {} (:constants full-ns))
        result-namespaces (reduce #(assoc %1 (:name %2) (build-ns %2 top-level settings)) result-constants tree-namespaces)

        result-functions (reduce #(assoc %1 (:name %2) (create-function %2 settings)) result-namespaces (:functions full-ns))

        result-classes (reduce #(assoc %1 (:name %2) (create-class %2 top-level settings))
                               result-functions
                               (get-classes top-level (map :name (:classes full-ns))))

        result-enums (reduce #(assoc %1 (:name %2) (create-enum %2 settings))
                             result-classes
                             (get-enums top-level (map :name (:enums full-ns))))

        result-typedefs (reduce #(assoc %1 (:name %2) (create-typedef %2 settings))
                                result-enums
                                (get-typedefs top-level (map :name (:typedefs full-ns))))
        result result-typedefs]
    (assoc result "!doc" (description full-ns)
                  "!url" (url ns settings))))


(defn build [ns top-level settings]
  {"!name"    "anychart"
   "anychart" (build-ns ns (update top-level :classes treefy-classes) settings)})


(defn test2 []
  (let [settings {:version-key "develop"
                  :domain      "http://api.anychart.stg/"}
        result (build (first @tree) @top-level settings)
        json (generate-string result {:pretty true})]
    (spit "/media/ssd/sibental/reference-engine-data/tern/anychart-generated.json" json)))


(defn generate-declarations [settings tree top-level]
  (info "generate TernJS definitions")
  (let [dir (str (:data-dir settings) "/versions-static/" (:version-key settings))
        file-name (str dir "/anychart-tern.json")
        file-name-min-js (str (:data-dir settings) "/versions-static/" (:version-key settings) "/def_anychart.min.js")

        result (build (first tree) top-level settings)

        json (generate-string result {:pretty true})
        json-min (generate-string result)
        js (str "var def_anychart = " json-min ";")]
    (fs/mkdirs dir)
    (spit file-name json)
    (spit file-name-min-js js)))