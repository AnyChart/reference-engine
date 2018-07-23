(ns reference.components.generator
  (:require [com.stuartsierra.component :as component]
            [reference.components.redis :as redisc]
            [reference.adoc.core :as gen]))


(defn generate-reference [comp gen-params]
  (gen/build-all (:jdbc comp)
                 (:notifier comp)
                 (:config comp)
                 (swap! (:queue-index (:config comp)) inc)
                 gen-params)
  (println "generate reference"))


(defn- message-processor [comp]
  (fn [{:keys [message attempt]}]
    (let [{cmd :cmd} message]
      (if (= cmd "generate")
        (generate-reference comp message)))
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
