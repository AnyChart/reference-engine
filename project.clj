(defproject reference "3.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [version-clj "0.1.2"]
                 ;; general
                 [com.stuartsierra/component "0.3.2"]
                 [clj-time "0.14.2"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.8.0"]
                 ;; templates
                 [selmer "1.11.7"]
                 ;; web
                 [http-kit "2.2.0"]
                 [compojure "1.6.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.6.3"]
                 [enlive "1.1.6"]
                 ;; logging
                 [com.taoensso/timbre "4.10.0"]
                 ;; databases
                 [com.taoensso/carmine "2.11.1"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [honeysql "0.9.2"]
                 [com.rpl/specter "1.1.0"]
                 [instaparse "1.4.8"]
                 [toml "0.1.3"]]
  :profiles {:dev {:jvm-opts ["-Ddev=true"]}
             :uberjar {:jvm-opts []}}
  :plugins [[lein-ancient "0.6.10"]]
  :main reference.core)
