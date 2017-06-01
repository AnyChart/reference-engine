(defproject reference "3.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.443"]
                 [version-clj "0.1.2"]
                 ;; general
                 [com.stuartsierra/component "0.3.2"]
                 [clj-time "0.13.0"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.7.1"]
                 ;; templates
                 [selmer "0.8.2"]
                 ;; web
                 [http-kit "2.2.0"]
                 [compojure "1.6.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.6.1"]
                 [enlive "1.1.6"]
                 ;; logging
                 [com.taoensso/timbre "4.10.0"]
                 ;; databases
                 [com.taoensso/carmine "2.11.1"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.2"]
                 [honeysql "0.6.3"]]
  :profiles {:dev {:jvm-opts ["-Ddev=true"]}
             :uberjar {:jvm-opts []}}
  :plugins [[lein-ancient "0.6.10"]]
  :main reference.core)
