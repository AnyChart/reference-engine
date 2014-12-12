(ns reference.generator.samples
  (:require [reference.config :as config]
            [clojure.java.io :refer [file]]
            [clojure.java.shell :refer [sh with-sh-dir]]
            [clj-jgit.porcelain :as git]
            [reference.generator.git :refer [auth-git] :as git-cli]
            [taoensso.timbre :as timbre :refer [info debug]]))

(defn- line-chart-example [code]
  (debug "line-chart-example")
  (str "chart = anychart.lineChart();\n"
       code
       "\nchart.container(stage).draw()"))

(defn- simple-h100-example [code]
  (debug "simple-h100-example")
  (str "stage.height(100);\n"
       code))

(defn- stage-only-example [code]
  (debug "stage-only-example")
  (str "var layer = acgraph.layer();\n"
       "stage.width(600);\n"
       code
       "\nlayer.parent(stage);"))

(defn- sample-file-content [code]
  (str "{:tags []\n"
" :exports \"stage\"}\n"
"anychart.onDocumentReady(function() {\n"
"  stage = anychart.graphics.create('container', 400, 300);\n"
code
"\n  document.getElementById('container').style.width = stage.width() + 'px';\n"
"  document.getElementById('container').style.height = stage.height() + 'px;'\n"
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
    (debug "saving sample" entry-name name version)
    (spit (str (base-path version) name ".sample")
          (sample-file-content code))
    (str "http://playground.anychart.stg/acdvf-reference/" version "/" name)))

(defn update []
  (info "update samples repo")
  (git-cli/update-samples (str config/data-path "samples-repo")))

(defn checkout-version [version]
  (info "checkout samples version" version)
  
  (if (.exists (file (base-path version)))
    (sh "rm" "-rf" (base-path version)))
  
  (sh "cp" "-r"
      (str config/data-path "samples-repo")
      (base-path version))
      
  (git-cli/checkout-or-create (base-path version) version)
  
  (let [samples-files (.listFiles (file (base-path version)))]
    (info "removing all sample files" (base-path version))
    (doall (map #(.delete %)
                (filter #(not (.isHidden %)) samples-files)))))

(defn commit-version [version]
  (info "commit samples" version)
  (git-cli/commit-and-push (base-path version) version))

(defn parse-sample [entry-name example version]
  (debug "parse-sample" entry-name version)
  (let [c (last (re-matches #"(?s).*<c>([^<]*)</c>.*" example))
        t (last (re-matches #"(?s).*<t>([^<]*)</t>.*" example))
        raw-code (clojure.string/trim-newline
                  (clojure.string/trim
                   (clojure.string/replace example #"<[ct]>([^<]*)</[ct]>" "")))
        code (case t
               "lineChart" (line-chart-example raw-code)
               "simple-h100" (simple-h100-example raw-code)
               "stageOnly" (stage-only-example raw-code)
               raw-code)
        link (if-not (= t "listingOnly")
               (save-sample (clojure.string/replace entry-name #"#" ".")
                            code version))]
    {:caption c
     :has-caption (boolean c)
     :link link
     :has-link (boolean link)
     :has-topbar (or (boolean c) (boolean link))
     :t t
     :listring-only? (= t "listingOnly")
     :code code}))
