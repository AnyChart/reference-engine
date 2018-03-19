(ns reference.adoc.typedef-builder
  (:require [reference.adoc.defs.ts.typescript :as ts]
            [com.rpl.specter :refer :all]
            [clojure.string :as string]))


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
        params-str (string/join ","
                                (map (fn [param]
                                       (str (string/join "|" (:types param)) " " (:name param)))
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


(defn fix-typedef [top-level & [is-ts]]
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
                                                           (re-pattern (:full-name typedef))
                                                           ((if-not is-ts
                                                              function-definition-js
                                                              function-definition-ts) typedef)))
                                         type
                                         typedefs)))]
    ;(prn (count typedefs))
    ;(prn (count function-typedefs))

    (assoc
      (transform [:classes ALL :methods ALL :overrides ALL :params ALL :types ALL] typedef-transform-fn top-level)
      :typedefs typedefs)))


