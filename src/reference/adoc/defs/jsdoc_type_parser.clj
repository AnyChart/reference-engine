(ns reference.adoc.defs.jsdoc-type-parser
  (:require [instaparse.core :as insta]))

;; =====================================================================================================================
;; Parser
;; =====================================================================================================================
(def parser
  (insta/parser
    "
     types = (<'('> types_ <')'>) | types_
     <types_> = type (<whitespace>? <'|'> <whitespace>? type)*

     <type> = (<'('> type_ <')'>) | type_
     <type_> = (simple|array|object|tsfunc|jsfunc)

     tsfunc = <'('> kvs? <')'> <'=>'> types

     jsfunc = <'function('> jsfuncparams <')'>  (<':'> types)?
     jsfuncparams = jsfuncparam? | jsfuncparam (<','> <whitespace>? jsfuncparam)*
     jsfuncparam = jsfuncparamname <':'> types
     jsfuncparamname = #'[a-zA-Z0-9_.]+'

     array = <'Array.<'> types <'>'>

     object = object-with-prefix | props
     <object-with-prefix> = <'Object.<'> (proptype <','> <whitespace>?) ? (types | props)  <'>'>
     proptype = types

     props = <'{'> kvs  <'}'> | (<'('> <'{'> kvs  <'}'> <')'>)
     <kvs> = kv (<','> <whitespace>? kv)*
     kv = key <':'> <whitespace>? types
     <key> = #'[a-zA-Z0-9_]+'

     <simple> = #'[a-zA-Z0-9_.*]+' | 'function()'

     whitespace = #'\\s+'
    "))
