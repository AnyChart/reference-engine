(ns reference.components.notifier
  (:require [com.stuartsierra.component :as component]
            [cheshire.core :refer [generate-string]]
            [org.httpkit.client :as http]))

(defrecord Notifier [config]
  component/Lifecycle

  (start [this] this)
  (stop [this] this))

(defn new-notifier [config]
  (map->Notifier {:config config}))

(defn delete-branches [notifier])

(defn start-building [notifier])

(defn complete-building [notifier])

(defn versions-for-build [notifier versions])

(defn start-version-building [notifier version])

(defn complete-version-building [notifier version])
