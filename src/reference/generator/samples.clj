(ns reference.generator.samples
  (:require [reference.config :as config]
            [clojure.java.io :refer [file]]))

(defn- line-chart-example [code]
  (str "var chart = anychart.lineChart();\n"
       code
       "chart.container(stage).draw()"))

(defn- simple-h100-example [code]
  (str "stage.height(100);\n"
       code))

(defn- stage-only-example [code]
  (str "var layer = acgraph.layer();\n"
       "stage.width(600);\n"
       code
       "layer.parent(stage);"))

(defn- sample-file-content [code]
  (str "{:tags []\n"
" :exports \"chart\"}\n"
"$('#container').width(600).height(400);\n\n"
"anychart.onDocumentReady(function() {\n"
"  var container ='container';\n"
"  var stage = anychart.graphics.create(container, 400, 300);\n"
code
"\n});"))

(defn- base-path [version]
  (str config/data-path "samples-versions/" version "/"))

(defn- sample-path [entry-name version index]
  (str (base-path version)
       entry-name
       "-e"
       (if (> index 0)
         index)
       ".sample"))

(defn- get-sample-path [entry-name version]
  (loop [index 0 path (sample-path entry-name version 0)]
    (if (not (.exists (file path)))
      path
      (recur (+ index 1) (sample-path entry-name version (+ index 1))))))

(defn- save-sample [entry-name code version]
  (let [path (get-sample-path entry-name version)]
    (spit path (sample-file-content code))))

(defn parse-sample [entry-name example version]
  (let [c (re-matches #".*<c>([^<]*)</c>.*" example)
        t (re-matches #".*<t>([^<]*)</t>.*" example)
        raw-code (clojure.string/replace example #"<[ct]>([^<]*)</[ct]>" "")
        code (case t
               "lineChart" (line-chart-example raw-code)
               "simple-h100" (simple-h100-example raw-code)
               "stageOnly" (stage-only-example raw-code)
               raw-code)]
    (if-not (= t "listingOnly")
      (save-sample (clojure.string/replace entry-name #"#" ".")
                   code version))
    {:caption (last c)
     :t (last t)
     :listring-only? (= t "listingOnly")
     :code code}))
