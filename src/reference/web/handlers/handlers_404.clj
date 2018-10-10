(ns reference.web.handlers.handlers-404
  (:require [reference.web.views.page404.page404 :as page-404]
            [reference.web.helpers :refer :all]
            [reference.components.notifier :as notifications]
            [ring.util.request :refer [request-url]]
            [compojure.route :as route]))


(defn show-404 [request]
  (let [data {:title       "Not found | AnyChart API Reference"
              :description "Not found page"
              :commit      (:commit (config request))}]
    (page-404/page data)))


(defn error-404 [request]
  (let [referrer (get-in request [:headers "referer"])
        ua (get-in request [:headers "user-agent"])]
    (when (not (.contains ua "Slackbot"))
      (if referrer
        (notifications/notify-404 (notifier request) (str (request-url request) " from " referrer))
        (notifications/notify-404 (notifier request) (request-url request)))))
  (show-404 request))
