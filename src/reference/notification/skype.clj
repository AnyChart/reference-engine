(ns reference.notification.skype
  (:require [org.httpkit.client :as http]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json]
            [reference.util.utils :as utils]
            [clojure.string :as s]
            [clojure.string :as string]
            [reference.config.core :as c]))


;; =====================================================================================================================
;; Settings and util functions
;; =====================================================================================================================
(defn- config [notifier] (-> notifier :config :skype))


(defn get-access-token [id key]
  (let [url "https://login.microsoftonline.com/common/oauth2/v2.0/token"
        data {"client_id"     id
              "scope"         "https://api.botframework.com/.default"
              "grant_type"    "client_credentials"
              "client_secret" key}
        resp @(http/post url {:form-params data})
        body (json/parse-string (:body resp) true)
        access-token (:access_token body)]
    access-token))


(defn send-msg [chat-id access-token message]
  (let [url (str "https://apis.skype.com/v2/conversations/" chat-id "/activities")
        data {:message {:content message}}
        headers {"Authorization" (str "Bearer " access-token)}
        resp @(http/post url {:body    (json/generate-string data)
                              :headers headers})]))


(defn send-message [{:keys [id key chat-id]} message]
  (try
    (let [access-token (get-access-token id key)]
      (send-msg chat-id access-token message))
    (catch Exception e
      (timbre/error "Skype send message error: " message))))


(defn send-release-message [conf message]
  (when (:release-chat-id conf)
    (send-message (assoc conf :chat-id (:release-chat-id conf)) message)))


(defn font [text & [color size]]
  (str "<font "
       (when color (str "color=\"" color "\" "))
       (when size (str "size=\"" size "px\"")) ">"
       text "</font>"))


(defn b [text] (str "<b>" text "</b>"))
(defn u [text] (str "<u>" text "</u>"))
(defn i [text] (str "<i>" text "</i>"))


;; =====================================================================================================================
;; Notifications functions
;; =====================================================================================================================
(defn start-version-building [notifier {author :author commit-message :message version :name} queue-index]
  (let [msg (str "#" queue-index " api " (-> (c/prefix) (font "#cc0066" 11) u) " - "
                 (b version)
                 " " commit-message " - " author
                 (-> " start" (font "#4183C4") b) "\n")]
    (send-message (config notifier) msg)
    (when (utils/released-version? version)
      (send-release-message (config notifier) msg))))


(defn complete-version-building [notifier version queue-index message]
  (let [msg (str "#" queue-index " api " (-> (c/prefix) (font "#cc0066" 11) u) " - " (b version) (-> " complete" (font "#36a64f") b) " " message "\n")]
    (send-message (config notifier) msg)
    (when (utils/released-version? version)
      (send-release-message (config notifier) msg))))


(defn build-failed [notifier version queue-index e ts-error]
  (let [msg (str "#" queue-index " api " (-> (c/prefix) (font "#cc0066" 11) u) " - " (b version) (-> " failed" (font "#d00000") b) "\n"
                 (when e
                   (-> (utils/format-exception e) (font "#777777" 11) i))
                 (when ts-error
                   (str "TypeScript generation errors: \n" (:url ts-error) "\n"
                        "" (:out ts-error) "")))]
    (send-message (config notifier) msg)
    (when (utils/released-version? version)
      (send-release-message (config notifier) msg))))


(defn complete-building-with-errors [notifier branches queue-index e]
  (let [msg (str "#" queue-index " api " (-> (c/prefix) (font "#cc0066" 11) u) " - " (-> "error during processing!" (font "#d00000") b) "\n"
                 (when (seq branches)
                   (str (b "Branches: ") (string/join ", " branches)))
                 (when e
                   (-> (utils/format-exception e) (font "#777777" 11) i)))]
    (send-message (config notifier) msg)))
