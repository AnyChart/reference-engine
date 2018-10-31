(ns reference.adoc.defs.ts.type-parser-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [reference.adoc.defs.jsdoc-type-parser :as type-parser]))


(deftest object-test
  (testing "Objects test"
    (is
      (= (type-parser/parser
           "Object.<string,number>|
            Object.<number>|
            Object.<Array.<string>>|
            {a:number,b:number}|
            Object.<{a:number,b:string}>|
            Object.<number,{a:number,b:number}>")
         [:types
          [:object [:proptype [:types "string"]] [:types "number"]]
          [:object [:types "number"]]
          [:object [:types [:array [:types "string"]]]]
          [:object [:props [:kv "a" [:types "number"]] [:kv "b" [:types "number"]]]]
          [:object [:props [:kv "a" [:types "number"]] [:kv "b" [:types "string"]]]]
          [:object [:proptype [:types "number"]] [:props [:kv "a" [:types "number"]] [:kv "b" [:types "number"]]]]]
         ))))


(deftest array-test
  (testing "Objects test"
    (is
      (= (type-parser/parser
           "Array|
            Array.<string>|
            Array.<string|number>")
         [:types
          "Array"
          [:array [:types "string"]]
          [:array [:types "string" "number"]]]))))



;; http://api.anychart.stg/develop/anychart.charts.Sunburst.StatsFieldsName
(def statsFieldsNameRaw
  "Object.<{sum: number,
   nodesCount: number,
   leavesCount: number,
   branchesCount: number,
   display: boolean,
   attendingRoots: (Array.<number>),
   statsByRoot: (Object.<string, ({sum: number,
                                   nodesCount: number,
                                   leavesSum: number,
                                   leavesCount: number,
                                   branchesCount: number,
                                   childSum: number,
                                   nodes: Array.<anychart.core.TreeChartPoint>})>
                                   )
   }>")

(def statsFieldsName (string/replace statsFieldsNameRaw #"\s+" ""))

(deftest statsFieldsNameTest
  (testing "StatsFieldsName test"
    (is
      (= (type-parser/parser statsFieldsName)
         [:types
          [:object
           [:props
            [:kv "sum" [:types "number"]]
            [:kv "nodesCount" [:types "number"]]
            [:kv "leavesCount" [:types "number"]]
            [:kv "branchesCount" [:types "number"]]
            [:kv "display" [:types "boolean"]]
            [:kv "attendingRoots" [:types [:array [:types "number"]]]]
            [:kv
             "statsByRoot"
             [:types
              [:object
               [:proptype [:types "string"]]
               [:props
                [:kv "sum" [:types "number"]]
                [:kv "nodesCount" [:types "number"]]
                [:kv "leavesSum" [:types "number"]]
                [:kv "leavesCount" [:types "number"]]
                [:kv "branchesCount" [:types "number"]]
                [:kv "childSum" [:types "number"]]
                [:kv "nodes" [:types [:array [:types "anychart.core.TreeChartPoint"]]]]]]]]]]]))))




;; ttp://localhost:8080/develop/anychart.core.resource.TimeLine.Level
(def timeline-level-raw "Object.<{fill: (anychart.graphics.vector.Fill|undefined), padding: (Object|Array.<number>|number|string|null|undefined), minFontSize: (number|undefined), maxFontSize: (number|undefined), adjustFontSize: (boolean|Array.<boolean>|{width:boolean, height:boolean}|undefined), fontSize: (number|undefined), fontFamily: (string|undefined), fontColor: (string|undefined), fontOpacity: (number|undefined), fontDecoration: (string|undefined), fontStyle: (anychart.enums.FontStyle|string|undefined), fontVariant: (anychart.enums.FontVariant|string|undefined), fontWeight: (string|number|undefined), letterSpacing: (number|string|undefined), textDirection: (string|undefined), lineHeight: (number|string|undefined), textIndent: (number|undefined), vAlign: (anychart.enums.VAlign|string|undefined), hAlign: (anychart.enums.HAlign|string|undefined), wordWrap: (string|undefined), wordBreak: (string|undefined), textOverflow: (anychart.graphics.vector.Text.TextOverflow|string|undefined), selectable: (boolean|undefined), disablePointerEvents: (boolean|undefined), useHtml: (boolean|undefined), format: (function()|undefined), f:string}>")
(def timeline-level (string/replace timeline-level-raw #"\s+" ""))

(deftest timeline-level-test
  (testing "timeline.Level test"
    (is
      (= (type-parser/parser timeline-level)
         [:types
          [:object
           [:props
            [:kv "fill" [:types "anychart.graphics.vector.Fill" "undefined"]]
            [:kv "padding" [:types "Object" [:array [:types "number"]] "number" "string" "null" "undefined"]]
            [:kv "minFontSize" [:types "number" "undefined"]]
            [:kv "maxFontSize" [:types "number" "undefined"]]
            [:kv
             "adjustFontSize"
             [:types
              "boolean"
              [:array [:types "boolean"]]
              [:object [:props [:kv "width" [:types "boolean"]] [:kv "height" [:types "boolean"]]]]
              "undefined"]]
            [:kv "fontSize" [:types "number" "undefined"]]
            [:kv "fontFamily" [:types "string" "undefined"]]
            [:kv "fontColor" [:types "string" "undefined"]]
            [:kv "fontOpacity" [:types "number" "undefined"]]
            [:kv "fontDecoration" [:types "string" "undefined"]]
            [:kv "fontStyle" [:types "anychart.enums.FontStyle" "string" "undefined"]]
            [:kv "fontVariant" [:types "anychart.enums.FontVariant" "string" "undefined"]]
            [:kv "fontWeight" [:types "string" "number" "undefined"]]
            [:kv "letterSpacing" [:types "number" "string" "undefined"]]
            [:kv "textDirection" [:types "string" "undefined"]]
            [:kv "lineHeight" [:types "number" "string" "undefined"]]
            [:kv "textIndent" [:types "number" "undefined"]]
            [:kv "vAlign" [:types "anychart.enums.VAlign" "string" "undefined"]]
            [:kv "hAlign" [:types "anychart.enums.HAlign" "string" "undefined"]]
            [:kv "wordWrap" [:types "string" "undefined"]]
            [:kv "wordBreak" [:types "string" "undefined"]]
            [:kv "textOverflow" [:types "anychart.graphics.vector.Text.TextOverflow" "string" "undefined"]]
            [:kv "selectable" [:types "boolean" "undefined"]]
            [:kv "disablePointerEvents" [:types "boolean" "undefined"]]
            [:kv "useHtml" [:types "boolean" "undefined"]]
            [:kv "format" [:types [:jsfunc [:jsfuncparams]] "undefined"]]
            [:kv "f" [:types "string"]]]]]))))



