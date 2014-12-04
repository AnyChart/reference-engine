(defproject reference "0.2.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.taoensso/carmine "2.7.0"]
                 [version-clj "0.1.0"]
                 ;; templates
                 [de.ubercode.clostache/clostache "1.4.0"]
                 ;; web
                 [http-kit "2.1.16"]
                 [compojure "1.1.9"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-core "1.3.1"]
                 ;; jgit
                 [clj-jgit "0.8.2"]
                 ;;frontend
                 [org.clojure/clojurescript "0.0-2371"]
                 [reagent "0.4.3"]]
  :plugins [[lein-cljsbuild "1.0.3"]]
  :cljsbuild {:builds [{:source-paths ["src-cljs"]
                        :compiler {:output-to "resources/public/min/reference.js"
                                   :optimizations :whitespace
                                   :preamble ["reagent/react.js"]}}]}
  :main reference.handler)
