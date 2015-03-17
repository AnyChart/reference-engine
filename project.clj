(defproject reference "1.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.taoensso/carmine "2.9.0"]
                 [version-clj "0.1.0"]
                 ;; templates
                 [de.ubercode.clostache/clostache "1.4.0"]
                 ;; web
                 [http-kit "2.1.16"]
                 [compojure "1.1.9"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-core "1.3.1"]
                 [enlive "1.1.5"]
                 ;; jgit
                 [clj-jgit "0.8.2"]
                 ;; logging
                 [com.taoensso/timbre "3.3.1"]
                 ;;frontend
                 [org.clojure/clojurescript "0.0-3123"]
                 [reagent "0.5.0"]
                 [weasel "0.6.0"]]
  :plugins [[lein-cljsbuild "1.0.5"]]
  :profiles {:dev {:jvm-opts ["-Ddev=true"]}
             :uberjar {:jvm-opts []}}
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/app.js"
                                   :optimizations :whitespace
                                   :preamble ["reagent/react.js"]}}
                       {:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/js/app.min.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :externs ["externs/ace.js"]
                                   :preamble ["reagent/react.js"]}}]}
  :main reference.handler)
