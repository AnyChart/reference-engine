(ns reference.config.spec
  (:require [clojure.spec.alpha :as s]))


(s/def :config.redis.spec/port pos-int?)
(s/def :config.redis.spec/host string?)
(s/def :config.redis.spec/db int?)
(s/def :config.redis/spec (s/keys :req-un [:config.redis.spec/port
                                           :config.redis.spec/host
                                           :config.redis.spec/db]))
(s/def :config/redis (s/keys :req-un [:config.redis/spec]))


(s/def :config.notifications.slack/channel string?)
(s/def :config.notifications.slack/token string?)
(s/def :config.notifications.slack/username string?)
(s/def :config.notifications/slack (s/keys :req-un [:config.notifications.slack/channel
                                                    :config.notifications.slack/token
                                                    :config.notifications.slack/username]))


(s/def :config.notifications.skype/id string?)
(s/def :config.notifications.skype/chat-id string?)
(s/def :config.notifications.skype/release-chat-id string?)
(s/def :config.notifications.skype/key string?)
(s/def :config.notifications/skype (s/keys :req-un [:config.notifications.skype/id
                                                    :config.notifications.skype/chat-id
                                                    :config.notifications.skype/key
                                                    :config.notifications.skype/release-chat-id]))

(s/def :config/notifications (s/keys :req-un [:config.notifications/skype
                                              :config.notifications/slack]))


(s/def :config.jdbc/subprotocol string?)
(s/def :config.jdbc/password string?)
(s/def :config.jdbc/classname string?)
(s/def :config.jdbc/subname string?)
(s/def :config.jdbc/user string?)
(s/def :config.jdbc/stringtype string?)
(s/def :config/jdbc (s/keys :req-un [:config.jdbc/subprotocol
                                     :config.jdbc/password
                                     :config.jdbc/classname
                                     :config.jdbc/subname
                                     :config.jdbc/user
                                     :config.jdbc/stringtype]))

(s/def :config.generator/show-branches boolean?)
(s/def :config.generator/git-ssh string?)
(s/def :config.generator/data-dir string?)
(s/def :config.generator/max-processes pos-int?)
(s/def :config.generator/jsdoc-bin string?)
(s/def :config.generator/queue string?)
(s/def :config/generator (s/keys :req-un [:config.generator/show-branches
                                          :config.generator/git-ssh
                                          :config.generator/data-dir
                                          :config.generator/max-processes
                                          :config.generator/jsdoc-bin
                                          :config.generator/queue]))


(s/def :config.web/debug boolean?)
(s/def :config.web/static pos-int?)
(s/def :config.web/port pos-int?)
(s/def :config.web/max-line pos-int?)
(s/def :config.web/reference-queue string?)
(s/def :config.web/docs string?)
(s/def :config.web/playground string?)
(s/def :config/web (s/keys :req-un [:config.web/debug
                                    :config.web/static
                                    :config.web/port
                                    :config.web/max-line
                                    :config.web/reference-queue
                                    :config.web/docs
                                    :config.web/playground]))


(s/def :config.common/prefix string?)
(s/def :config.common/domain string?)
(s/def :config/common (s/keys :req-un [:config.common/prefix
                                       :config.common/domain]))


(s/def ::config (s/keys :req-un [:config/common
                                 :config/web
                                 :config/generator
                                 :config/jdbc
                                 :config/notifications
                                 :config/redis]))