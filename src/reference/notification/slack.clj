(ns reference.notification.slack
  (:require [org.httpkit.client :as http]
            [cheshire.core :refer [generate-string]]
            [reference.util.utils :as utils]
            [clojure.string :as string]
            [reference.config.core :as c]))


(defn- channel [notifier] (-> notifier :config :slack :channel))
(defn- username [notifier] (-> notifier :config :slack :username))
(defn- token [notifier] (-> notifier :config :slack :token))


(defn- notify [notifier text]
  (http/post (str "https://anychart-team.slack.com/services/hooks/incoming-webhook?token=" (token notifier))
             {:form-params
              {:payload (generate-string
                          {:text     (str (c/domain) " " text)
                           :channel  (channel notifier)
                           :username (username notifier)})}}))


(defn- notify-attach [notifier attachments]
  (http/post (str "https://anychart-team.slack.com/services/hooks/incoming-webhook?token=" (token notifier))
             {:form-params
              {:payload (generate-string
                          {:attachments attachments
                           :mrkdwn      true
                           :channel     (channel notifier)
                           :username    (username notifier)})}}))


(defn start-building [notifier branches removed-branches queue-index]
  (let [attachments [{:color     "#4183C4"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - start")
                      :mrkdwn_in ["text", "pretext"]
                      :fields    (if (seq branches)
                                   [{:title "Branches"
                                     :value (clojure.string/join ", " branches)
                                     :short true}]
                                   [])}]
        removed-fields (when (seq removed-branches)
                         [{:title "Removed branches"
                           :value (clojure.string/join ", " removed-branches)
                           :short true}])]
    (notify-attach notifier (update-in attachments [0 :fields] concat removed-fields))))


(defn complete-building [notifier branches removed-branches queue-index]
  (let [attachments [{:color     "#36a64f"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - complete")
                      :mrkdwn_in ["text", "pretext"]
                      :fields    (if (seq branches)
                                   [{:title "Branches"
                                     :value (clojure.string/join ", " branches)
                                     :short true}]
                                   [])}]
        removed-fields (when (seq removed-branches)
                         [{:title "Removed branches"
                           :value (clojure.string/join ", " removed-branches)
                           :short true}])]
    (notify-attach notifier (update-in attachments [0 :fields] concat removed-fields))))


(defn complete-building-with-errors [notifier branches queue-index e]
  (let [attachments [{:color     "danger"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - complete with errors"
                                      (when e (str "\n```" (utils/format-exception e) "```")))
                      :mrkdwn_in ["text", "pretext"]
                      :fields    (if (seq branches)
                                   [{:title "Branches"
                                     :value (clojure.string/join ", " branches)
                                     :short true}]
                                   [])}]]
    (notify-attach notifier attachments)))


(defn start-version-building [notifier version queue-index]
  (let [attachments [{:color     "#4183C4"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - *" version "* start")
                      :mrkdwn_in ["text"]}]]
    (notify-attach notifier attachments)))


(defn complete-version-building [notifier version queue-index]
  (let [attachments [{:color     "#36a64f"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - *" version "* complete")
                      :mrkdwn_in ["text"]}]]
    (notify-attach notifier attachments)))


(defn complete-version-building-error [notifier version queue-index e ts-error]
  (let [attachments [{:color     "danger"
                      :text      (str "#" queue-index " api `" (c/prefix) "` - *" version "* failed"
                                      (when e (str "\n```" (utils/format-exception e) "```"))
                                      (when ts-error
                                        (str "\nTypeScript generation errors:\n" (:url ts-error) "\n"
                                             "```"
                                             (:out ts-error)
                                             "```")))
                      :mrkdwn_in ["text"]}]]
    (notify-attach notifier attachments)))


(defn notify-404 [notifier path]
  (http/post (str "https://anychart-team.slack.com/services/hooks/incoming-webhook?token=" (token notifier))
             {:form-params
              {:payload (generate-string
                          {:text     (str (c/domain) " 404: " path)
                           :channel  "#api-404-errors"
                           :username (username notifier)})}}))
