(ns reference.components.generator
  (:require [com.stuartsierra.component :as component]))

(defn- generate-reference [comp]
  (println "generate reference"))

(defn- message-processor [comp]
  (fn [{:keys [message attempt]}]
    (if (= message "generate")
      (generate-reference comp))
    {:status :success}))

(defrecord Worker [config jdbc redis notifier]
  component/Lifecycle

  (start [this]
    (assoc this :engine (redisc/create-worker redis
                                              (-> this :config :queue)
                                              (message-processor this))))

  (stop [this]
    (redisc/delete-worker (:engine this))
    (dissoc this :engine)))

(defn new-worker [config]
  (map->Worker {:config config}))
