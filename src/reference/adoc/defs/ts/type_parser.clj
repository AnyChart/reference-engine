(ns reference.adoc.defs.ts.type-parser
  (:require [instaparse.core :as insta]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))

;; =====================================================================================================================
;; Parser
;; =====================================================================================================================
(def parser
  (insta/parser
    "
     types = (<'('> types_ <')'>) | types_
     <types_> = type (<whitespace>? <'|'> <whitespace>? type)*

     <type> = (<'('> type_ <')'>) | type_
     <type_> = (simple|array|object|tsfunc)

     tsfunc = <'('> kvs <')'> <'=>'> types

     array = <'Array.<'> types <'>'>

     object = object-with-prefix | props
     <object-with-prefix> = <'Object.<'> (proptype <','>) ? (types | props)  <'>'>
     proptype = types

     props = <'{'> kvs  <'}'> | (<'('> <'{'> kvs  <'}'> <')'>)
     <kvs> = kv (<','> kv)*
     kv = key <':'> types
     <key> = #'[a-zA-Z]+'

     <simple> = #'[a-zA-Z0-9.]+' | 'function()'

     whitespace = #'\\s+'
    "))


;; =====================================================================================================================
;; Writer
;; =====================================================================================================================
(defmulti write first)


(defmethod write :default [data]
  (case data
    "Array" "Array<any>"
    "function" "(() => void)"
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


(defmethod write :types [[_ & types]]
  (->> types
       (map write)
       (string/join "|")))


;; =====================================================================================================================
;; JsDoc => TS
;; =====================================================================================================================
(defn jsdoc->ts [s]
  (let [data (parser s)]
    (if (insta/failure? data)
      (do
        (timbre/info "TS parse error:" (pr-str s))
        (timbre/info (insta/get-failure data))
        s)
      (write data))))
