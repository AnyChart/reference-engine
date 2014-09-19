(ns reference-engine.db
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.java.io :refer (file)]))

(def server-conn {:pool {} :spec {:host "127.0.0.1" :port 6379}})
(defmacro wcar* [& body] `(car/wcar server-conn ~@body))
