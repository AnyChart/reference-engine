(ns reference.generator.samples
  (:require [reference.config :as config]
            [clojure.java.io :refer [file]]
            [clojure.java.shell :refer [sh]]
            [clj-jgit.porcelain :as git]
            [reference.generator.git :refer [auth-git] :as git-cli]))

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

(defn- sample-name [entry-name index]
  (str entry-name
       "-e"
       (if (> index 0)
         index)))

(defn- get-sample-name [entry-name version]
  (loop [index 0
         name (sample-name entry-name 0)
         path (sample-path entry-name version 0)]
    (if (not (.exists (file path)))
      name
      (recur (+ index 1)
             (sample-name entry-name (+ index 1))
             (sample-path entry-name version (+ index 1))))))

(defn- save-sample [entry-name code version]
  (let [name (get-sample-name entry-name version)]
    (spit (str (base-path version) name ".sample")
          (sample-file-content code))
    (str "{{PLAYGROUND}}/acdvf-reference/" version "/" name)))

(defn update []
  (git-cli/update-samples (str config/data-path "samples-repo")))

(defn checkout-version [version]
  (if (.exists (file (base-path version)))
    (sh "rm" "-rf" (base-path version)))
  
  (sh "cp" "-r"
      (str config/data-path "samples-repo")
      (base-path version))

  (let [repo (git/load-repo (base-path version))
        branches (git/git-branch-list repo)
        branch-names (map #(git-cli/get-branch-name %) branches)]
    (if (some #{version} branch-names)
      (git/git-checkout repo version)
      (do
        (git/git-branch-create repo version)
        (git/git-checkout repo version)))))

(defn commit-version [version]
  (git-cli/commit-and-push (base-path version) version))

(defn parse-sample [entry-name example version]
  (let [c (re-matches #".*<c>([^<]*)</c>.*" example)
        t (re-matches #".*<t>([^<]*)</t>.*" example)
        raw-code (clojure.string/trim-newline
                  (clojure.string/trim
                   (clojure.string/replace example #"<[ct]>([^<]*)</[ct]>" "")))
        code (case t
               "lineChart" (line-chart-example raw-code)
               "simple-h100" (simple-h100-example raw-code)
               "stageOnly" (stage-only-example raw-code)
               raw-code)]
    {:caption (last c)
     :link (if-not (= t "listingOnly")
             (save-sample (clojure.string/replace entry-name #"#" ".")
                          code version))
     :t (last t)
     :listring-only? (= t "listingOnly")
     :code code}))
