(ns reference.components.notifier
  (:require [com.stuartsierra.component :as component]
            [cheshire.core :refer [generate-string]]
            [reference.notification.skype :as skype]
            [reference.notification.slack :as slack]))


(defrecord Notifier [config]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))


(defn new-notifier [config]
  (map->Notifier {:config config}))


(defn start-building [notifier branches removed-branches queue-index]
  (slack/start-building notifier branches removed-branches queue-index))


(defn complete-building [notifier branches removed-branches queue-index]
  (slack/complete-building notifier branches removed-branches queue-index))


(defn complete-building-with-errors [notifier branches queue-index & [e]]
  (slack/complete-building-with-errors notifier branches queue-index e)
  ;(skype/complete-building-with-errors notifier branches queue-index e)
  )


(defn start-version-building [notifier branch queue-index]
  (slack/start-version-building notifier (:name branch) queue-index)
  (skype/start-version-building notifier branch queue-index))


(defn complete-version-building [notifier version queue-index]
  (slack/complete-version-building notifier version queue-index)
  (skype/complete-version-building notifier version queue-index "good job, everything is ok!"))


(defn complete-version-building-error [notifier version queue-index e ts-error]
  (slack/complete-version-building-error notifier version queue-index e ts-error)
  (skype/complete-version-building-error notifier version queue-index e ts-error))


(defn notify-404 [notifier path]
  (slack/notify-404 notifier path))
