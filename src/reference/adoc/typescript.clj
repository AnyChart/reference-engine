(ns reference.adoc.typescript
  (:require [clojure.string :as s :refer [join]]
            [taoensso.timbre :as timbre :refer [info error]]))

(defonce ^:const p4 "    ")
(defonce ^:const p8 "        ")

;; for developing
(defonce top-level (atom nil))

(defn set-top-level! [_top-level]
  (reset! top-level _top-level))

(defn get-type [t]
  ; "Object.<string, (string|boolean)>" => "{[prop: string]: string|boolean}"
  (if-let [m (re-matches #"Object\.<([^>,]*),\s*([^>,]*)\>" t)]
    (let [[_ key-type value-type] m]
      (str "{[prop: " key-type "]: " value-type "}"))
    (case t
      "function" "(() => void)"
      (-> t
          (s/replace #"scope" "any")
          (s/replace #"\*" "any")
          (s/replace #"Array." "Array")
          (s/replace #"!" "")))))

(defn get-types [types]
  (join " | " (map get-type types)))

;;; =============== functions ===============

(defn function-return [returns]
  (if (seq returns)
    (let [types (mapcat :types returns)]
      (join " | " (map get-type types)))
    "void"))

(defn function-param [param]
  (str (:name param) ": " (function-return [param])))

(defn function-params [params]
  (join ", " (map function-param params)))

(defn function-declaration [f]
  (str p4 "function " (:name f) "(" (function-params (:params f)) "): " (function-return (:returns f)) ";"))

(defn function-declarations [functions]
  (join "\n" (map function-declaration functions)))

;;; =============== constants ===============

(defn constant-declaration [constant]
  (str p4 "const " (:name constant) (when (:type constant) (str ": " (:type constant))) ";"))

(defn constant-declarations [constants]
  (join "\n" (map constant-declaration constants)))

;;; =============== typedefs ===============

(defn interface-prop [prop]
  (str p8 (:name prop) ": "  (join " | " (map get-type (:type prop))) ";" ))

(defn interface-props [props]
  (join "\n" (map interface-prop props)))

(defn typedef-declaration [typedef]
  (if (empty? (:properties typedef))
    (str p4 "type " (:name typedef) " = " (get-types (:type typedef))  ";")
    (str p4 "interface " (:name typedef) " {\n"
         (interface-props (:properties typedef))
         "\n    }")))

(defn typedef-declarations [typedefs]
  (join "\n" (map typedef-declaration typedefs)))

(defn get-typedefs [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:typedefs top-level)))

;;; ============= enums ======================

(defn enum-field [field]
  (if (integer? (:value field))
    (str p8 (:name field) " = " (:value field) "" )
    (str p8 (:name field) " = <any>\"" (:value field) "\"" )))

(defn enum-declaration [enum]
  (str p4 "enum " (:name enum) " {\n"
        (join ",\n" (map enum-field (:fields enum)))
       "\n    }"))

(defn enum-declarations [enums]
  (join "\n" (map enum-declaration enums)))

(defn get-enums [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:enums top-level)))

;;; ========= classes ==========

(defn method-declaration [f]
  (str p8 (:name f) "(" (function-params (:params f)) "): " (function-return (:returns f)) ";"))

(defn class-declaration [class]
  (if (> (.indexOf (:name class) ".") 0)
    (let [[module name] (s/split (:name class) #"\.")]
      (str p4 "module " module " {\n"
           (class-declaration (assoc class :name name))
           "\n    }"))
    (str p4 "interface " (:name class)
         (when (:extends class) (str " extends " (join ", " (:extends class)))) " {\n"
         (join "\n" (map method-declaration (mapcat :overrides (:methods class))))
         "\n    }")))

(defn class-declarations [classes]
  (join "\n" (map class-declaration classes)))

(defn get-classes [top-level names]
  (filter (fn [td] (some #(= % (:full-name td) ) names)) (:classes top-level)))

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

       (class-declarations (get-classes top-level (map :name (:classes namespace))))
       (when (seq (:classes namespace)) "\n")
       "}"))

;(defn test2 []
;  (let [namespaces (join "\n\n" (map #(namespace-definition @top-level %) (sort-by :full-name (:namespaces @top-level))))]
;    (spit "/media/ssd/work/TypeScript/St1/src/anychart.d.ts" namespaces)))

(defn generate-ts-declarations [data-dir version-key top-level]
  (info "generate TypeScript definitions")
  (let [file-name (str data-dir "/versions-static/" version-key "/anychart.d.ts")
        namespaces (join "\n\n" (map #(namespace-definition top-level %) (sort-by :full-name (:namespaces top-level))))]
    (spit file-name namespaces)))