(ns reference.web.helpers)


(defn config [request]
  (-> request :component :config))

(defn static-version [request]
  (-> request :component :config :static))

(defn jdbc [request]
  (-> request :component :jdbc))

(defn redis [request]
  (-> request :component :redis))

(defn notifier [request]
  (-> request :component :notifier))