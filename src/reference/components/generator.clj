(ns reference.components.generator
  (:require [com.stuartsierra.component :as component]
            [reference.components.redis :as redisc]
            [reference.adoc.core :as gen]
            [taoensso.timbre :as timbre :refer [info error debug]]
            [clojure.stacktrace]))


(defn generate-reference [comp gen-params]
  (debug "DEBUG: Starting reference generation with params:" gen-params)
  (debug "DEBUG: Component config:" (:config comp))
  (try
    (gen/build-all (:jdbc comp)
                   (:notifier comp)
                   (:config comp)
                   (swap! (:queue-index (:config comp)) inc)
                   gen-params)
    (debug "DEBUG: Reference generation completed successfully")
    (println "generate reference")
    (catch Exception e
      (error e "Exception during reference generation")
      (debug "DEBUG: Exception details:" (with-out-str (clojure.stacktrace/print-stack-trace e))))))


(defn- message-processor [comp]
  (fn [{:keys [message attempt]}]
    (debug "DEBUG: Processing message:" message "attempt:" attempt)
    (let [{cmd :cmd} message]
      (debug "DEBUG: Command received:" cmd)
      (if (= cmd "generate")
        (do
          (debug "DEBUG: Executing generate command")
          (generate-reference comp message)
          (debug "DEBUG: Generate command completed"))
        (debug "DEBUG: Unknown command:" cmd)))
    {:status :success}))


(defrecord Generator [config jdbc redis notifier]
  component/Lifecycle
  (start [this]
    (assoc this :engine (redisc/create-worker redis
                                              (-> this :config :queue)
                                              (message-processor this))))
  (stop [this]
    (redisc/delete-worker (:engine this))
    (dissoc this :engine)))


(defn new-generator [config]
  (map->Generator {:config (assoc config :queue-index (atom 0))}))
