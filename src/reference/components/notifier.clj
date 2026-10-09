(ns reference.components.notifier
  (:require [com.stuartsierra.component :as component]
            [cheshire.core :refer [generate-string]]
            ;; ToDo: add notifications
  )
)


(defrecord Notifier [config]
  component/Lifecycle
  (start [this] this)
  (stop [this] this))


(defn new-notifier [config]
  (map->Notifier {:config config}))


(defn start-building [notifier branches removed-branches queue-index]
  ;; ToDo: add notifications
)


(defn complete-building [notifier branches removed-branches queue-index]
  ;; ToDo: add notifications
)


(defn complete-building-with-errors [notifier branches queue-index & [e]]
  ;; ToDo: add notifications
  )


(defn start-version-building [notifier branch queue-index]
  ;; ToDo: add notifications
)


(defn complete-version-building [notifier branch queue-index dts-enabled]
  ;; ToDo: add notifications
)


(defn complete-version-building-error [notifier branch queue-index e ts-error]
  ;; ToDo: add notifications
)


(defn notify-404 [notifier path]
  ;; ToDo: add notifications
)
