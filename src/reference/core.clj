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


(defn dev-system [config]
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


(def all-config nil)

(def config nil)

(def stg-config nil)

(def prod-config)


(defn set-configs [config-path]
  (alter-var-root #'all-config (constantly (toml/read (slurp config-path) :keywordize)))
  (alter-var-root #'config (constantly (-> all-config :base)))
  (alter-var-root #'stg-config (constantly (utils/deep-merge config (:stg all-config))))
  (alter-var-root #'prod-config (constantly (utils/deep-merge config (:prod all-config)))))


(def dev (dev-system config))


(defn start []
  (alter-var-root #'dev component/start))


(defn stop []
  (alter-var-root #'dev component/stop))


(defn -main
  ([] (println "dev backend|stg frontend|stg backend|com frontend|com backend"))
  ([mode config-path]
   (set-configs config-path)
   (if (= mode "dev")
     (component/start (dev-system config))
     (timbre/info "Unknown mode")))
  ([domain mode config-path]
   (set-configs config-path)
   (cond
     (and (= domain "stg") (= mode "frontend")) (component/start (frontend-system stg-config))
     (and (= domain "stg") (= mode "backend")) (component/start (generator-system stg-config))
     (and (= domain "com") (= mode "frontend")) (component/start (frontend-system prod-config))
     (and (= domain "com") (= mode "backend")) (component/start (generator-system prod-config))
     :else (timbre/info "Unknown domain or mode"))))