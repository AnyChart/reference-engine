(ns reference.notification.discord
  (:require [org.httpkit.client :as http]
            [cheshire.core :refer [generate-string]]
            [reference.util.utils :as utils]
            [clojure.string :as string]
            [reference.config.core :as c]
            [taoensso.timbre :as timbre]))

;; =====================================================================================================================
;; Config and Helpers
;; =====================================================================================================================
(defn webhook-url [notifier] (-> notifier :config :discord :webhook-url))
(defn username [notifier] (-> notifier :config :discord :username))

;; Text formatting
(defn b [text] (str "**" text "**"))
(defn code-block [text] (str "```\n" text "\n```"))

;; Base notification function
(defn- send-webhook [url data]
  (try
    @(http/post url
                {:body (generate-string data)
                 :headers {"Content-Type" "application/json"}})
    (catch Exception e
      (timbre/error "Discord webhook error:" (.getMessage e)))))

(defn- notify [notifier embeds]
  (let [payload {:username (username notifier)
                 :embeds embeds}]
    (send-webhook (webhook-url notifier) payload)))

;; =====================================================================================================================
;; Slack Analog Functions
;; =====================================================================================================================

(defn start-building [notifier branches removed-branches queue-index]
  (let [embed {:color 4437377 ; Blue
               :title (str "#" queue-index " api `" (c/prefix) "` - start")
               :fields (cond-> []
                        (seq branches)
                        (conj {:name "Branches"
                              :value (string/join ", " branches)
                              :inline true})
                        (seq removed-branches)
                        (conj {:name "Removed branches"
                              :value (string/join ", " removed-branches)
                              :inline true}))}]
    (notify notifier [embed])))

(defn complete-building [notifier branches removed-branches queue-index]
  (let [embed {:color 3066993 ; Green
               :title (str "#" queue-index " api `" (c/prefix) "` - complete")
               :fields (cond-> []
                        (seq branches)
                        (conj {:name "Branches"
                              :value (string/join ", " branches)
                              :inline true})
                        (seq removed-branches)
                        (conj {:name "Removed branches"
                              :value (string/join ", " removed-branches)
                              :inline true}))}]
    (notify notifier [embed])))

(defn complete-building-with-errors [notifier branches queue-index e]
  (let [embed {:color 15158332 ; Red
               :title (str "#" queue-index " api `" (c/prefix) "` - complete with errors")
               :description (when e (code-block (utils/format-exception e)))
               :fields (when (seq branches)
                        [{:name "Branches"
                          :value (string/join ", " branches)
                          :inline true}])}]
    (notify notifier [embed])))

(defn start-version-building [notifier version queue-index]
  (let [embed {:color 4437377 ; Blue
               :description (str "#" queue-index " api `" (c/prefix) "` - " (b version) " start")}]
    (notify notifier [embed])))

(defn complete-version-building [notifier version queue-index]
  (let [embed {:color 3066993 ; Green
               :description (str "#" queue-index " api `" (c/prefix) "` - " (b version) " complete")}]
    (notify notifier [embed])))

(defn complete-version-building-error [notifier version queue-index e {:keys [index-ts-result graphics-ts-result]}]
  (let [embed {:color 15158332 ; Red
               :description (str "#" queue-index " api `" (c/prefix) "` - " (b version) " failed"
                                (when e (str "\n" (code-block (utils/format-exception e))))
                                (when (not= 0 (:exit index-ts-result))
                                  (str "\n[index.d.ts](" (:url index-ts-result) ") errors"
                                       (when (:count index-ts-result)
                                         (str " - " (b (:count index-ts-result)) " tests failed"))
                                       ":\n"
                                       (code-block (:out index-ts-result))))
                                (when (not= 0 (:exit graphics-ts-result))
                                  (str "\n[graphics.d.ts](" (:url graphics-ts-result) ") errors"
                                       (when (:count graphics-ts-result)
                                         (str " - " (b (:count graphics-ts-result)) " tests failed"))
                                       ":\n"
                                       (code-block (:out graphics-ts-result)))))}]
    (notify notifier [embed])))

(defn notify-404 [notifier path]
  (let [embed {:color 15158332 ; Red
               :description (str (c/domain) " 404: " path)}]
    (notify notifier [embed])))

;; =====================================================================================================================
;; Unique Skype Functions
;; =====================================================================================================================

(defn- config [notifier] (-> notifier :config :discord))

;; Additional formatting for Skype-style messages
(defn i [text] (str "*" text "*"))
(defn u [text] (str "__" text "__"))

(defn send-release-message [conf version message]
  (when (and (:release-webhook-url conf)
             (utils/released-version? version)
             (= (c/prefix) "prod"))
    (let [payload {:username (username conf)
                  :content message}]
      (send-webhook (:release-webhook-url conf) payload))))

;; Extended version building functions with commit info
(defn start-version-building-with-commit
  [notifier {:keys [author message name commit] :as version-info} queue-index]
  (let [msg (str "[API " (c/prefix) "] #" queue-index " " (b name)
                 " \"" message "\" @" author " (" (subs commit 0 7) ") - start")
        embed {:color 4437377
               :description msg}]
    (notify notifier [embed])
    (send-release-message (config notifier) name msg)))

(defn complete-version-building-with-commit
  [notifier {:keys [author message name commit] :as version-info} queue-index dts-enabled]
  (let [msg (str "[API " (c/prefix) "] #" queue-index " " (b name)
                 " \"" message "\" @" author " (" (subs commit 0 7) ") - complete"
                 (when-not dts-enabled " (d.ts generation OFF)"))
        embed {:color 3066993
               :description msg}]
    (notify notifier [embed])
    (send-release-message (config notifier) name msg)))

(defn complete-version-building-error-with-commit
  [notifier {:keys [author message name commit] :as version-info} queue-index e {:keys [index-ts-result graphics-ts-result]}]
  (let [msg (str "[API " (c/prefix) "] #" queue-index " " (b name)
                 " \"" message "\" @" author " (" (subs commit 0 7) ") - failed"
                 (when e (str "\n" (code-block (utils/format-exception e))))
                 (when (not= 0 (:exit index-ts-result))
                   (str "\n[index.d.ts](" (:url index-ts-result) ") errors"
                        (when (:count index-ts-result)
                          (str " - " (b (:count index-ts-result)) " tests failed"))
                        ":\n"
                        (code-block (:out index-ts-result))))
                 (when (not= 0 (:exit graphics-ts-result))
                   (str "\n[graphics.d.ts](" (:url graphics-ts-result) ") errors"
                        (when (:count graphics-ts-result)
                          (str " - " (b (:count graphics-ts-result)) " tests failed"))
                        ":\n"
                        (code-block (:out graphics-ts-result)))))
        embed {:color 15158332
               :description msg}]
    (notify notifier [embed])
    (send-release-message (config notifier) name msg)))
```

Configuration required:
```clojure
{:discord {:webhook-url "https://discord.com/api/webhooks/your-webhook-url"
           :release-webhook-url "https://discord.com/api/webhooks/your-release-webhook-url"
           :username "API Bot"}}
