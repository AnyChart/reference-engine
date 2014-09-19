(ns reference-engine.config)

(def git-ssh (if (not (System/getProperty "dev"))
               "/apps/reference/keys/git"
               "/Users/alex/Work/anychart/reference-engine/resources/keys/git"))
