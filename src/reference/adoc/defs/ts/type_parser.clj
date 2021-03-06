(ns reference.adoc.defs.ts.type-parser
  (:require [reference.adoc.defs.jsdoc-type-parser :as jsdoc-type-parser]
            [instaparse.core :as insta]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))


;; =====================================================================================================================
;; Writer
;; =====================================================================================================================
(defmulti write first)


(defmethod write :default [data]
  (case data
    "Array" "Array<any>"
    "function" "(() => void)"
    "*" "any"
    nil "void"
    data))


(defmethod write :kv [[_ key value]]
  (str key ":" (write value)))


(defmethod write :props [[_ & kvs]]
  (str "{" (->> kvs
                (map write)
                (string/join ",")) "}"))


(defmethod write :array [[_ data]]
  (str "Array<" (write data) ">"))


(defmethod write :object [[_ & data]]
  (let [proptype? (= :proptype (first (first data)))
        types? (= :types (first (first data)))]
    (if (or proptype? types?)
      (str "{[prop:"
           (if proptype? (write (second (first data))) "string")
           "]:"
           (write (last data))
           "}")
      (write (last data)))))


(defmethod write :tsfunc [[_ & kvs-return]]
  (let [kvs (butlast kvs-return)
        return (last kvs-return)]
    (str "((" (string/join "," (map write kvs)) ")=>" (write return) ")")))


(defmethod write :jsfunc [[_ [_ & js-func-params] return]]
  (str "((" (string/join "," (map write js-func-params)) ")=>" (write return) ")"))


(defmethod write :jsfuncparam [[_ [_ paramname] types]]
  (str paramname ":" (write types)))


(defmethod write :types [[_ & types]]
  (->> types
       (map write)
       (string/join "|")))


;; =====================================================================================================================
;; JsDoc => TS
;; =====================================================================================================================
(defn jsdoc->ts [s]
  (let [data (jsdoc-type-parser/parser s)]
    (if (insta/failure? data)
      (do
        (timbre/info "TS parse error:" (pr-str s))
        (timbre/info (insta/get-failure data))
        s)
      (write data))))
