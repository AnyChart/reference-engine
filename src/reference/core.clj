(ns reference.core
  (:require [reference.components.jdbc :as jdbc]
            [reference.components.redis :as redis]
            [reference.components.notifier :as notifier]
            [reference.components.generator :as generator]
            [reference.components.web :as web]
            [reference.config.core :as c]
            [com.stuartsierra.component :as component]
            [reference.util.utils :as utils]
            [toml.core :as toml]
            [taoensso.timbre :as timbre]
            [reference.git :as git])
  (:gen-class))


(defn git-commit []
  (try
    (git/current-commit "/apps/keys/git" (.getAbsolutePath (clojure.java.io/file "")))
    (catch Exception _ (quot (System/currentTimeMillis) 1000))))


(defmacro parse-data-compile-time []
  `'~(git-commit))


(def commit (parse-data-compile-time))


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


(defn update-config [conf]
  (-> conf
      (assoc-in [:web :commit] commit)))


(defn set-config [config-path]
  (let [config-data (toml/read (slurp config-path) :keywordize)]
    (if (c/check-config config-data)
      (do
        (alter-var-root #'config (constantly (update-config config-data)))
        (c/set-config config-data))
      (do
        (timbre/error (c/explain-config config-data))
        (System/exit 1)))))


(def dev (all-system config))
(def system nil)

(defn start []
  (alter-var-root #'dev component/start))


(defn stop []
  (alter-var-root #'dev component/stop))


(defn -main
  ([] (println "all|backend|frontend config-path"))
  ([mode config-path]
   (println "Starting with mode:" mode "config path:" config-path)
   (timbre/info "Starting with mode:" mode "config path:" config-path)
   (timbre/debug "DEBUG: Starting application with mode:" mode "config path:" config-path)
   (set-config config-path)
   (cond
     (= mode "all") 
     (do
       (timbre/debug "DEBUG: Starting in 'all' mode with full system")
       (alter-var-root #'system (constantly (component/start (all-system config)))))
     
     (= mode "frontend") 
     (do
       (timbre/debug "DEBUG: Starting in 'frontend' mode")
       (component/start (frontend-system config)))
     
     (= mode "backend") 
     (do
       (timbre/debug "DEBUG: Starting in 'backend' mode")
       (timbre/debug "DEBUG: This mode processes data from repo and populates the database")
       (let [result (component/start (generator-system config))]
         (timbre/debug "DEBUG: Backend system started:" (if result "success" "failure"))
         (timbre/debug "DEBUG: If you're having database population issues, check:")
         (timbre/debug "DEBUG: 1. Redis connection status")
         (timbre/debug "DEBUG: 2. Git repository path and access")
         (timbre/debug "DEBUG: 3. PostgreSQL connection and schema")
         result))
     
     :else (timbre/info "Unknown mode")))
  
  ;; Special version for direct database triggering
  ([mode config-path version]
   (println "Starting direct DB population with mode:" mode "config path:" config-path "version:" version)
   (timbre/debug "DEBUG: Starting direct DB population with version:" version)
   (set-config config-path)
   (let [sys (generator-system config)
         running-sys (component/start sys)]
     (timbre/debug "DEBUG: Directly triggering database population for version:" version)
     (generator/generate-reference (:generator running-sys) {:cmd "generate" :version version})
     (timbre/debug "DEBUG: Database population completed")
     (component/stop running-sys))))