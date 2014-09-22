(defproject reference-engine "0.1"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.taoensso/carmine "2.7.0"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [juxt/dirwatch "0.2.0"]
                 [http-kit "2.1.16"]
                 [compojure "1.1.9"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-core "1.3.1"]]
  :profiles {:dev {:jvm-opts ["-Ddev=true"]}
             :uberjar {:jvm-opts []}})
