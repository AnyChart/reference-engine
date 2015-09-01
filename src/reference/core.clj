(ns reference.core
  (:require [reference.components.jdbc :as jdbc]
            [reference.components.redis :as redis]
            [reference.components.notifier :as notifier]
            [reference.components.generator :as generator]
            [reference.components.web :as web]
            [com.stuartsierra.component :as component])
  (:gen-class :main :true))

(defn dev-system [config]
  (component/system-map
   :notifier (notifier/new-notifier (:notifications config))
   :jdbc  (jdbc/new-jdbc (:jdbc config))
   :redis (redis/new-redis (:redis config))
   :web   (component/using (web/new-web (:web config))
                           [:jdbc :redis])
   :generator (component/using (generator/new-generator (:generator config))
                               [:jdbc :redis :notifier])))

(defn frontend-system [config]
  (component/system-map
   :jdbc  (jdbc/new-jdbc (:jdbc config))
   :redis (redis/new-redis (:redis config))
   :web   (component/using (web/new-web (:web config))
                           [:jdbc :redis])))

(defn generator-system [config]
  (component/system-map
   :notifier (notifier/new-notifier (:notifications config))
   :jdbc  (jdbc/new-jdbc (:jdbc config))
   :redis (redis/new-redis (:redis config))
   :generator (component/using (generator/new-generator (:generator config))
                               [:jdbc :redis :notifier])))

;; CREATE USER reference_user WITH PASSWORD 'pass';
;; CREATE DATABASE reference_db;
;; GRANT ALL PRIVILEGES ON DATABASE reference_db TO reference_user;
;; psql -p5432 -d reference_db -U reference_user -W

(def base-config
  {:notifications {:token "P8Z59E0kpaOqTcOxner4P5jb"
                   :channel "#notifications"
                   :username "reference-engine"
                   :domain "http://localhost/"}
   :web {:debug true
         :static 12
         :port 8080
         :reference-queue "reference-queue"
         :docs "docs.anychart.stg"
         :playground "playground.anychart.stg"}
   :jdbc {:subprotocol "postgresql"
          :subname "//localhost:5432/reference_db"
          :classname "org.postgresql.Driver"
          :user "reference_user"
          :password "pass"
          :stringtype "unspecified"}
   :redis {:pool {}
           :spec {:host "127.0.0.1" :port 6379 :db 0}}
   :generator {:show-branches true
               :git-ssh "/Users/alex/Work/anychart/reference-engine/keys/git"
               :data-dir (.getAbsolutePath (clojure.java.io/file "data"))
               :max-processes 8
               :jsdoc-bin "/usr/local/bin/jsdoc"
               :queue "reference-queue"}})

(def stg-config (merge-with merge base-config
                       {:notifications {:domain "http://api.anychart.stg/"}}
                       {:web {:debug false
                              :port 8090}}
                       {:jdbc {:subname "//10.132.9.26:5432/api_stg"
                               :user "api_stg_user"
                               :password "fuckstg"}}
                       {:redis {:spec {:host "10.132.9.26" :db 1}}}
                       {:generator {:git-ssh "/apps/keys/git"
                                    :data-dir "/apps/reference-stg/data"}}))

(def prod-config (merge-with merge base-config
                       {:notifications {:domain "http://api.anychart.com/"}}
                       {:web {:debug false
                              :port 8091
                              :reference-queue "reference-queue-prod"
                              :docs "docs.anychart.com"
                              :playground "playground.anychart.com"}}
                       {:jdbc {:subname "//10.132.9.26:5432/api_prod"
                               :user "api_prod_user"
                               :password "fuckprod"}}
                       {:redis {:spec {:host "10.132.9.26" :db 1}}}
                       {:generator {:show-branches false
                                    :git-ssh "/apps/keys/git"
                                    :data-dir "/apps/reference-prod/data"
                                    :queue "reference-queue-prod"}}))

(def config base-config)

(def dev (dev-system config))

(defn start []
  (alter-var-root #'dev component/start))

(defn stop []
  (alter-var-root #'dev component/stop))

(defn -main
  ([] (println "dev keys-path|stg frontend|stg backend|com frontend|com backend ??"))
  ([mode]
   (if (= mode "dev")
     (component/start (dev-system config))
     (println "started at http://localhost:8080")))
  ([domain mode]
   (cond
     (= domain "dev") (component/start (dev-system (assoc-in config [:generator :git-ssh] mode)))
     (and (= domain "stg") (= mode "frontend")) (component/start (frontend-system stg-config))
     (and (= domain "stg") (= mode "backend")) (component/start (generator-system stg-config))
     (and (= domain "com") (= mode "frontend")) (component/start (frontend-system prod-config))
     (and (= domain "com") (= mode "backend")) (component/start (generator-system prod-config)))))
