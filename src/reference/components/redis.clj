(ns reference.components.redis
  (:require [com.stuartsierra.component :as component]
            [taoensso.carmine :as car]
            [taoensso.carmine.message-queue :as car-mq]))

(defrecord Redis [config]
  component/Lifecycle
  (start [this]
    (assoc this :conn {:pool (:pool config)
                       :spec (:spec config)}))
  (stop [this]
    (assoc this :conn nil)))

(defn enqueue [redis queue message]
  (car/wcar (:config redis)
            (car-mq/enqueue queue message)))

(defn create-worker [redis queue handler]
  (car-mq/worker (:config redis) queue {:handler handler}))

(defn delete-worker [worker]
  (car-mq/stop worker))

(defn new-redis [config]
  (map->Redis {:config config}))
