(ns reference-engine.samples
  (:require [clojure.string :refer [trim]]))

(defn init-samples-repo [project version])

(defn parse-sample [project version obj-name sample]
  (let [t-info (last (re-find #"(?s)<t>(.*)</t>" sample))
        c-info (last (re-find #"(?s)<c>(.*)</c>" sample))
        code (trim (last (re-find #"(?s)((<t>(.*)</t>)|(<c>(.*)</c>))*(.*)" sample)))]
    {:listing-only? (= t-info "listingOnly")
     :title c-info
     :code code}))

