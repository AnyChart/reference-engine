(ns reference.web.handlers.versions-handlers
  (:require [reference.data.versions :as vdata]
            [reference.web.helpers :refer :all]
            [ring.util.response :refer [response redirect header content-type]]))


(defn list-versions [request]
  (response (vdata/versions (jdbc request))))