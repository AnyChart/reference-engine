(ns reference.adoc.typedef-builder
  (:require [reference.adoc.defs.ts.typescript :as ts]
            [com.rpl.specter :refer :all]
            [clojure.string :as string]
            [taoensso.timbre :as timbre]))


;; for developing
;(require [reference.adoc.typedef-builder :as tb])
; (tb/set-test-top-level @tb/top-level)
(defonce top-level (atom nil))

(defn set-top-level! [val] (reset! top-level val))

(defonce ^:const function-type "function")

(defonce mini-top-level (atom nil))


(defn set-test-top-level [top-level]
  (reset! mini-top-level {:typedefs   (filter (fn [typedef]
                                                (= function-type
                                                   (first (:type typedef)))) (:typedefs top-level))
                          :classes    (concat
                                        (filter (fn [class] (= "anychart.core.Base" (:full-name class))) (:classes top-level))
                                        [(first (:classes top-level))])
                          :enums      []
                          :namespaces []}))


(defn function-definition-js [typedef]
  (let [params (:params typedef)
        returns (:returns typedef)
        params-str (string/join ", "
                                (map (fn [param]
                                       (str (:name param) ":" (string/join "|" (:types param))))
                                     params))
        returns-str (string/join "|" (mapcat :types returns))]
    (str "function(" params-str ")"
         (when (seq returns) (str ":" returns-str)))))


(defn function-definition-ts [typedef]
  (let [params (:params typedef)
        returns (:returns typedef)
        params-str (string/join ","
                                (map (fn [param]
                                       (str (:name param) ":" (string/join "|" (:types param))))
                                     params))
        returns-str (string/join "|" (mapcat :types returns))]
    (str "((" params-str ") => "
         (if (seq returns)
           returns-str
           "void") ")")))


(defn fix-typedef [top-level & {is-ts :typescript}]
  (timbre/info "Fix function typedefs"
               (count (:namespaces top-level))
               (count (:classes top-level))
               (count (:typedefs top-level))
               (count (:enums top-level)))
  (let [all-typedefs (:typedefs top-level)
        group-typedefs (group-by (fn [typedef]
                                   (= function-type
                                      (first (:type typedef)))) all-typedefs)
        typedefs (get group-typedefs false)
        function-typedefs (get group-typedefs true)

        ;function-typedef? (fn [type]
        ;                    (some (fn [typedef]
        ;                            (string/includes? type (:full-name typedef)))
        ;                          function-typedefs))

        typedef-transform-fn (fn [type]
                               (let [typedefs (filter
                                                (fn [typedef] (string/includes? type (:full-name typedef)))
                                                function-typedefs)]
                                 ;(prn type (map :full-name typedefs))
                                 (reduce (fn [res typedef]
                                           (string/replace res
                                                           ;; add lookahead and lookbehind
                                                           (re-pattern (str "(?<![a-zA-Z])"
                                                                            (:full-name typedef)
                                                                            "(?![a-zA-Z])"))
                                                           ((if is-ts
                                                              function-definition-ts
                                                              function-definition-js) typedef)))
                                         type
                                         typedefs)))]
    ;(prn (count typedefs))
    ;(prn (count function-typedefs))
    (assoc
      (->> top-level
           (transform [:classes ALL :methods ALL :overrides ALL :params ALL :types ALL] typedef-transform-fn)
           (transform [:classes ALL :methods ALL :overrides ALL :returns ALL :types ALL] typedef-transform-fn)
           (transform [:namespaces ALL :functions ALL :overrides ALL :params ALL :types ALL] typedef-transform-fn)
           (transform [:namespaces ALL :functions ALL :overrides ALL :returns ALL :types ALL] typedef-transform-fn))
      :typedefs typedefs)))