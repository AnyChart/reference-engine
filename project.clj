(defproject reference "3.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [version-clj "0.1.2"]
                 ;; general
                 [com.stuartsierra/component "0.3.2"]
                 [clj-time "0.14.4"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.8.0"]
                 ;; templates
                 [selmer "1.11.8"]
                 ;; web
                 [http-kit "2.3.0"]
                 [compojure "1.6.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.6.3"]
                 [enlive "1.1.6"]
                 ;; logging
                 [com.taoensso/timbre "4.10.0"]
                 ;; databases
                 [com.taoensso/carmine "2.18.1"]
                 [org.clojure/java.jdbc "0.7.7"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [honeysql "0.9.3"]
                 [com.rpl/specter "1.1.1"]
                 [instaparse "1.4.9"]
                 [toml "0.1.3"]
                 [hiccup "1.0.5"]
                 [garden "1.3.5"]]
  :profiles {:dev {:jvm-opts ["-Ddev=true"]}
             :uberjar {:jvm-opts []}}
  :plugins [[lein-ancient "0.6.10"]
            [deraen/sass4clj "0.3.1"]
            [deraen/lein-sass4clj "0.3.1"]
            [lein-asset-minifier "0.4.4"]]
  :sass {:source-paths ["src-scss"]
         :target-path  "resources/public/css"
         :output-style :compressed}

  :minify-assets [[:js {:source ["resources/public/lib/gemini/index.js"
                                 "resources/public/lib/gemini/pisces.js"]
                        :target "resources/public/lib/gemini/gemini.min.js"}]]

  :main reference.core)
