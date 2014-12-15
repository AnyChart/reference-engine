(ns data
  (:require [goog.net.XhrIo]
            [cljs.core.async :refer [chan put! take! timeout] :as async])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn load-json [version data]
  (let [c (chan 1)]
    (goog.net.XhrIo/send
     (str "/" version "/data/" data)
     (fn [e]
       (if (.isSuccess (.-target e))
         (let [res (.getResponseJson (.-target e))]
           (put! c (js->clj res :keywordize-keys true))))))
    c))
