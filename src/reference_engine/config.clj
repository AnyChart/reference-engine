(ns reference-engine.config)

(def git-ssh (if (not (System/getProperty "dev"))
               "/apps/playground/keys/git"
               "/Users/alex/Work/anychart/reference-engine/resources/keys/git"))

(def base-path (if (System/getProperty "dev")
                 "/Users/alex/Work/anychart/reference-engine"
                 "/apps/api"))

(def show-branches (not (System/getProperty "prod")))

(def data-path (str base-path "/data/"))

(def is-local (atom false))
