(defproject reference "3.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [version-clj "0.1.0"]
                 ;; general
                 [com.stuartsierra/component "0.2.3"]
                 [clj-time "0.11.0"]
                 [me.raynes/fs "1.4.6"]
                 ;; templates
                 [selmer "0.8.2"]
                 ;; web
                 [http-kit "2.1.16"]
                 [compojure "1.1.9"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-core "1.3.1"]
                 [enlive "1.1.5"]
                 ;; logging
                 [com.taoensso/timbre "4.0.1"]
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
