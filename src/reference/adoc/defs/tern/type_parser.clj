(ns reference.adoc.defs.tern.type-parser
  (:require [instaparse.core :as insta]
            [reference.adoc.defs.jsdoc-type-parser :as jsdoc-type-parser]
            [taoensso.timbre :as timbre]
            [clojure.string :as string]))

;; =====================================================================================================================
;; Writer
;; =====================================================================================================================
(defmulti write first)


(defmethod write :default [data]
  (case data
    "Array" "[?]"
    "function" "fn()"
    "*" "?"
    nil "void"
    data))


(defmethod write :kv [[_ key value]]
  (str key ": " (write value)))


(defmethod write :props [[_ & kvs]]
  (str "{" (->> kvs
                (map write)
                (string/join ", ")) "}"))


(defmethod write :array [[_ data]]
  (str "[" (write data) "]"))


(defmethod write :object [[_ & data]]
  (let [proptype? (= :proptype (first (first data)))
        types? (= :types (first (first data)))]
    ;(if (or proptype? types?)
    ;  (str "{[prop:"
    ;       (if proptype? (write (second (first data))) "string")
    ;       "]:"
    ;       (write (last data))
    ;       "}")
    ;  (write (last data)))
    (write (last data))))


(defmethod write :tsfunc [[_ & kvs-return]]
  (let [kvs (butlast kvs-return)
        return (last kvs-return)]
    (str "fn(" (string/join ", " (map write kvs)) ")"
         (when return
           (str " -> " (write return))))))


(defmethod write :jsfunc [[_ [_ & js-func-params] return]]
  (str "fn(" (string/join ", " (map write js-func-params)) ")"
       (when return
         (str " -> " (write return)))))


(defmethod write :jsfuncparam [[_ [_ paramname] types]]
  (str paramname ": " (write types)))


(defmethod write :types [[_ & types]]
  ;(let [anychart-types (filter #(string/starts-with? % "anychart.") types)]
  ;  (if (> (count anychart-types) 1)
  ;    "?"
  ;    (->> types
  ;         (map write)
  ;         (string/join "|"))))
  ;(prn types)
  (->> types
       (map write)
       (string/join "|")))


;; =====================================================================================================================
;; JsDoc => TS
;; =====================================================================================================================
(defn jsdoc->tern [s]
  (let [data (jsdoc-type-parser/parser s)]
    (if (insta/failure? data)
      (do
        (timbre/info "Tern parse error:" (pr-str s))
        (timbre/info (insta/get-failure data))
        s)
      (write data))))
