(ns reference.adoc.defs.ts.typescript
  (:require [clojure.string :as string :refer [join]]
            [taoensso.timbre :as timbre :refer [info error]]
            [me.raynes.fs :as fs]
            [reference.adoc.defs.ts.check :as ts-check]
            [reference.adoc.defs.ts.type-parser :as type-parser]
            [clojure.string :as string]))


(defonce ^:const p4 "    ")
(defonce ^:const p8 "        ")


;; for developing
(defonce top-level (atom nil))


(defn set-top-level! [_top-level]
  (reset! top-level _top-level))


(defn check-param [param]
  (case param
    "function" "func"
    "this" "obj"
    (string/replace param #"-" "")))


;(defn parse-object-type [t]
;  ; "Object.<string, (string|boolean)>" => "{[prop: string]: string|boolean}"
;  (if (.startsWith t "Object.<")
;
;    (if (.startsWith t "Object.<string,")
;      (if-let [m (re-matches #"Object\.<([^>,]*),\s*([^>]*)\>" t)]
;        (let [[_ key-type value-type] m]
;          (str "{[prop: " key-type "]: " value-type "}"))
;        (do
;          ;(prn t)
;          (str "{[prop: string]: " (string/trim (subs t 15 (dec (count t)))) "}")))
;
;      (parse-object-type (string/replace t #"Object\.<" "Object.<string, ")))
;    t))


;(defn get-type [t]
;  (let [t (parse-object-type (string/replace t #"\|undefined" ""))]
;    (case t
;      "function" "(() => void)"
;      "Array" "Array<any>"
;      (-> t
;          (s/replace #"function\(\)" "Function")
;          (s/replace #"scope" "any")
;          (s/replace #"\*" "any")
;          (s/replace #"Array\." "Array")
;          (s/replace #"!" "")
;          (s/replace #"\|null" "")))))



(defn get-type [t]
  (-> t
      (string/replace #"function\(\)" "Function")
      (string/replace #"scope" "any")
      (string/replace #"\*" "any")
      (string/replace #"!" "")
      (string/replace #"\|null" "")
      (string/replace #"\|undefined" "")
      (string/replace #"\s+" "")
      type-parser/jsdoc->ts))


(defn get-types [types]
  (->> types
       (map #(string/replace % #"anychart\.enums\.[a-zA-Z0-9]+" "string"))
       distinct
       (filter (partial #(and (not= % "null")
                              (not= % "undefined"))))
       (map get-type)
       (join " | ")))


;; =====================================================================================================================
;; Functions
;; =====================================================================================================================

(defn function-return [returns]
  (if (seq returns)
    (let [types (mapcat :types returns)]
      (get-types types))
    "void"))


(defn check-optional [param name]
  (if (:optional param)
    (str name "?")
    name))


(defn function-param [param]
  (if-not (:name param)
    (do (prn "pram" param) "_error_")
    (let [param-name (check-param (:name param))]
      (if (= param-name "var_args")
        (str "..." (check-optional param param-name) ": (" (function-return [param]) ")[]")
        (str (check-optional param param-name) ": " (function-return [param]))))))


(defn function-params [params]
  (join ", " (map function-param params)))


(defn function-declaration [f]
  (str p4 "function " (:name f) "(" (function-params (:params f)) "): " (function-return (:returns f)) ";"))


(defn function-declarations [functions]
  (join "\n" (map function-declaration (mapcat :overrides functions))))

;; =====================================================================================================================
;; Constants
;; =====================================================================================================================

(defn constant-declaration [constant]
  (str p4 "const " (:name constant) (when (:type constant) (str ": " (get-type (:type constant)))) ";"))


(defn constant-declarations [constants]
  (join "\n" (map constant-declaration constants)))

;; =====================================================================================================================
;; Typedefs
;; =====================================================================================================================


(defn interface-prop [prop]
  (str p8 (check-optional prop (check-param (:name prop))) ": " (get-types (:type prop)) ";"))


(defn interface-props [props]
  (join "\n" (map interface-prop props)))


(defn typedef-declaration [typedef]
  (if (empty? (:properties typedef))
    (str p4 "type " (:name typedef) " = " (get-types (:type typedef)) ";")
    (str p4 "type " (:name typedef) " = {\n"
         (interface-props (:properties typedef))
         "\n    }")))


(defn typedef-declarations [typedefs]
  (join "\n" (map typedef-declaration typedefs)))


(defn get-typedefs [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:typedefs top-level)))

;; =====================================================================================================================
;; Enums
;; =====================================================================================================================

(defn enum-field [field]
  (if (integer? (:value field))
    (str p8 (:name field) " = " (:value field) "")
    ;(str p8 (:name field) " = <any>\"" (:value field) "\"" )
    (str p8 (:name field))))


(defn enum-declaration [enum]
  (str p4 "enum " (:name enum) " {\n"
       (join ",\n" (map enum-field (:fields enum)))
       "\n    }"))


(defn enum-field-cl [field class-name]
  (str p8 "static " (:name field) " = new " class-name "(\"" (:value field) "\");"))


(defn enum-declaration-cl [enum]
  (str p4 "class " (:name enum) " {\n"
       p8 "constructor(public value:string){}\n"
       p8 "toString(){return this.value;}\n"
       (join "\n" (map #(enum-field-cl % (:name enum)) (:fields enum)))
       "\n    }"))


(defn enum-declarations [enums]
  (join "\n" (map enum-declaration enums)))


(defn get-enums [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:enums top-level)))

;; =====================================================================================================================
;; Classes
;; =====================================================================================================================

(defn get-enums-and-typedefs-class [class top-level]
  (when (or (seq (:enums class))
            (seq (:typedefs class)))
    (str
      ;"\n" p4 "module  " (:full-name class) " {\n"
      "\n" p4 "namespace " (:name class) " {\n"
      (enum-declarations (get-enums top-level (map :name (:enums class))))
      (typedef-declarations (get-typedefs top-level (map :name (:typedefs class))))
      "\n    }")))


(defn method-declaration [f]
  (str p8 (:name f) "(" (function-params (:params f)) "): " (function-return (:returns f)) ";"))


(defn class-declaration [class top-level]
  (if (> (.indexOf (:name class) ".") 0)
    (let [[module name] (string/split (:name class) #"\.")]
      (str p4 "module " module " {\n"
           (class-declaration (assoc class :name name) top-level)
           "\n    }"
           (get-enums-and-typedefs-class class top-level)
           ))
    (str p4 "interface " (:name class)
         (when (:extends class) (str " extends " (join ", " (:extends class)))) " {\n"
         (join "\n" (map method-declaration (mapcat :overrides (:methods class))))
         "\n    }"
         (get-enums-and-typedefs-class class top-level))))


(defn class-declarations [top-level classes]
  (join "\n" (map #(class-declaration % top-level) classes)))


(defn get-classes [top-level names]
  (filter (fn [td] (some #(= % (:full-name td)) names)) (:classes top-level)))


(defn namespace-definition [top-level namespace]
  (str "declare namespace " (:full-name namespace) " {\n"
       (constant-declarations (:constants namespace))
       (when (seq (:constants namespace)) "\n")

       (function-declarations (:functions namespace))
       (when (seq (:functions namespace)) "\n")

       (typedef-declarations (get-typedefs top-level (map :name (:typedefs namespace))))
       (when (seq (:typedefs namespace)) "\n")

       (enum-declarations (get-enums top-level (map :name (:enums namespace))))
       (when (seq (:enums namespace)) "\n")

       (class-declarations top-level (get-classes top-level (map :name (:classes namespace))))
       (when (seq (:classes namespace)) "\n")
       "}"))


(defn add-prefix [data]
  (str "/// <reference path=\"all.d.ts\"/>\n"
       "// from https://github.com/teppeis/closure-library.d.ts\n" data))


(defn generate-ts [top-level]
  (->> (:namespaces top-level)
       (remove #(= (:full-name %) "anychart.enums"))
       (sort-by :full-name)
       (map #(namespace-definition top-level %))
       (join "\n\n")))


(defn add-header [ts version-key]
  (str "// Type definitions for AnyChart JavaScript Charting Library" (str ", v" version-key)
       "\n// Project: https://www.anychart.com/\n"
       "// Definitions by: AnyChart <https://www.anychart.com>\n"
       ts))


(defn generate-ts-declarations [data-dir git-ssh version-key latest-version-key top-level notifier]
  (info "generate TypeScript definitions for: " version-key ", latest: " latest-version-key)
  ;; create two .d.ts files
  (let [is-last-version (= version-key latest-version-key)
        dir (str data-dir "/versions-static/" version-key)

        file-name-index "index.d.ts"
        file-name-full (str "index-" version-key ".d.ts")

        path-index (str dir "/" file-name-index)
        path-full (str dir "/" file-name-full)

        ts (generate-ts top-level)
        ts (add-header ts version-key)

        url (str (-> notifier :config :slack :domain) "si/" version-key "/" file-name-index)]
    (fs/mkdirs dir)
    (spit path-index ts)
    (spit path-full ts)
    {:url  url
     :path path-index}))


(defn add-graphics-js-header [ts version-key]
  (str "// Type definitions for GraphicsJS JavaScript Graphics Library" (str ", v" version-key)
       "\n// Project: http://www.graphicsjs.org/\n"
       "// Definitions by: AnyChart <https://www.anychart.com>\n"
       ts))


(defn generate-graphics-ts-declarations [data-dir git-ssh version-key latest-version-key top-level notifier]
  (info "generate GraphicsJS TS definitions for: " version-key ", latest: " latest-version-key)
  (let [is-last-version (= version-key latest-version-key)
        dir (str data-dir "/versions-static/" version-key)

        file-name-index "graphics.d.ts"
        file-name-full (str "graphics-" version-key ".d.ts")

        path-index (str dir "/" file-name-index)
        path-full (str dir "/" file-name-full)

        ts (generate-ts top-level)
        ts (string/replace ts "anychart.graphics" "acgraph")
        ts (add-graphics-js-header ts version-key)

        url (str (-> notifier :config :slack :domain) "si/" version-key "/" file-name-index)]
    (fs/mkdirs dir)
    (spit path-index ts)
    (spit path-full ts)
    {:url  url
     :path path-index}))


;(defn test2 []
;  (let [ts (generate-ts @top-level "develop" false)]
;    (spit "/media/ssd/work/TypeScript/St1/src/anychart.d.ts" ts)
;    (spit "/media/ssd/work/TypeScript/typescript-example/typescript-example/src/anychart.d.ts" ts)
;    (spit "/media/ssd/sibental/reference-engine-data/index.d.ts" ts)))