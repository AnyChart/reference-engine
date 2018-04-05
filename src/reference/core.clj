(ns reference.core
  (:require [reference.components.jdbc :as jdbc]
            [reference.components.redis :as redis]
            [reference.components.notifier :as notifier]
            [reference.components.generator :as generator]
            [reference.components.web :as web]
            [com.stuartsierra.component :as component]
            [reference.util.utils :as utils]
            [toml.core :as toml]
            [taoensso.timbre :as timbre])
  (:gen-class))


(defn all-system [config]
  (component/system-map
    :notifier (notifier/new-notifier (:notifications config))
    :jdbc (jdbc/new-jdbc (:jdbc config))
    :redis (redis/new-redis (:redis config))
    :web (component/using (web/new-web (:web config))
                          [:jdbc :redis :notifier])
    :generator (component/using (generator/new-generator (:generator config))
                                [:jdbc :redis :notifier])))


(defn frontend-system [config]
  (component/system-map
    :notifier (notifier/new-notifier (:notifications config))
    :jdbc (jdbc/new-jdbc (:jdbc config))
    :redis (redis/new-redis (:redis config))
    :web (component/using (web/new-web (:web config))
                          [:jdbc :redis :notifier])))


(defn generator-system [config]
  (component/system-map
    :notifier (notifier/new-notifier (:notifications config))
    :jdbc (jdbc/new-jdbc (:jdbc config))
    :redis (redis/new-redis (:redis config))
    :generator (component/using (generator/new-generator (:generator config))
                                [:jdbc :redis :notifier])))


;; CREATE USER reference_user WITH PASSWORD 'pass';
;; CREATE DATABASE reference_db;
;; GRANT ALL PRIVILEGES ON DATABASE reference_db TO reference_user;
;; psql -p5432 -d reference_db -U reference_user -W


(def config nil)


(defn set-config [config-path]
  (let [config-data (toml/read (slurp config-path) :keywordize)]
    (alter-var-root #'config (constantly config-data))))


(def dev (all-system config))


(defn start []
  (alter-var-root #'dev component/start))


(defn stop []
  (alter-var-root #'dev component/stop))


(defn -main
  ([] (println "all|backend|frontend config-path"))
  ([mode config-path]
   (set-config config-path)
   (cond
     (= mode "all") (component/start (all-system config))
     (= mode "frontend") (component/start (frontend-system config))
     (= mode "backend") (component/start (generator-system config))
     :else (timbre/info "Unknown mode"))))