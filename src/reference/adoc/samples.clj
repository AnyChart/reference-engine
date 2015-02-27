(ns reference.adoc.samples
  (:require [reference.config :as config]
            [clojure.java.io :refer [file]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- get-all-samples [path]
  (let [res (filter #(and
                      (re-matches #".*/_samples/.*\.html" (.getAbsolutePath %))
                      (not (.isHidden %)))
                    (file-seq (file path)))]
    (info "samples count:" (count res))
    res))

(defn- sample-meta [exports]
  (str "{:tags [] :is_new false :exports " (if exports exports "chart") "}\n"))

(defn- prettify-code [code]
  (clojure.string/replace code
                          #"[ ]{8}"
                          ""))

(defn- sample-code [script-node]
  (prettify-code (apply str (:content script-node))))                        

(defn- process-sample [sample-file base-path version]
  (let [base-folder (clojure.string/replace (.getParent sample-file)
                                            (clojure.string/re-quote-replacement base-path)
                                            "")
        target-folder (clojure.string/replace base-folder #"[/]*_samples" "")
        target-relative-path (str target-folder "/" (.getName sample-file))
        target-path (str config/samples-path version "/" target-relative-path)
        page (html/html-resource sample-file)
        script-node (first (filter #(not (:src (:attrs %))) (html/select page [:script])))
        exports (:x-export (:attrs script-node))
        generated-sample (str (sample-meta exports) (sample-code script-node))]
    (info "sample:" generated-sample)))

(defn process-samples [version]
  (info "process samples" version)
  (let [src-path (str config/versions-path version "/")
        samples (get-all-samples src-path)]
    (doall (map #(process-sample % src-path version) samples))))

(process-samples "develop")
