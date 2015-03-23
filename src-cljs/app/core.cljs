(ns app.core
  (:require [reagent.core :as reagent :refer [atom]]
            [app.versions :refer [init-versions-events]]
            [app.tree :refer [load-tree]]
            [app.search :refer [load-search-index]]
            [app.resize :refer [init-resize]]
            [app.ajax :as ajax]
            [app.page]
            [weasel.repl :as ws-repl]))

(enable-console-print!)

(defn connect-repl []
  (ws-repl/connect "ws://localhost:9001"))

(defn- init-events []
  (init-versions-events)
  (init-resize))

(defn ^:export init [version page info]
  (init-events)
  ;;(editors/init-editors)
  (ajax/init-navigation)
  (load-tree version)
  (load-search-index version)
  (app.page/init version page (js->clj info)))

;;(.reinit js/window)
