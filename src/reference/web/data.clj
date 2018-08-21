(ns reference.web.data
  (:require [reference.web.views.main.entries.enum :as enum-view]
            [reference.web.views.main.entries.typedef :as typedef-view]
            [reference.web.views.main.entries.class-entry :as class-view]
            [reference.web.views.main.entries.namespace-entry :as namespace-view]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [parse-string]]
            [reference.web.tree :refer [tree-view-partial]]
            [clojure.string :as string]
            [hiccup.core :as hiccup]))


(defn fix-version [html version]
  (string/replace html "__VERSION__" version))


(defn fix-links [html version is-url-version]
  (string/replace html
                  #"\{@link (anychart[^\}]+)\}"
                  (str "<a class='type-link' href='/"
                       (when is-url-version (str version "/"))
                       "$1'>$1</a>")))


(defn fix-web-links [html]
  (string/replace html
                  #"\{@link ([^\}]+)\}"
                  (str "<a class='type-link' href='$1'>$1</a>")))


(defn fix-docs-links [html docs-domain version is-url-version]
  (string/replace html
                  #"\{docs:([^\}]+)\}([^\{]+)\{docs\}"
                  (str "<a href='//" docs-domain
                       (when is-url-version (str "/" version))
                       "/$1'>$2</a>")))


(defn fix-api-links [html version is-url-version]
  (string/replace html
                  #"\{api:([^\}]+)\}([^\{]+)\{api\}"
                  (str "<a href='"
                       (when is-url-version (str "/" version))
                       "/$1'>$2</a>")))


(defn fix-pg-links [html playground-domain version is-url-version]
  (string/replace html
                  #"\{pg:([^\}]+)\}([^\{]+)\{pg\}"
                  (str "<a href='//" playground-domain "/api"
                       (when is-url-version (str "/" version))
                       "/$1'>$2</a>")))


(defn render-entry [docs-domain playground-domain version-key show-samples entry-type entry is-url-version]
  (let [data {:main           entry
              :version        version-key
              :is-url-version is-url-version
              :show-samples   true
              :playground     playground-domain}
        html (case entry-type
               "enum" (hiccup/html (enum-view/enum data))
               "typedef" (hiccup/html (typedef-view/typedef data))
               "class" (hiccup/html (class-view/class' data))
               "namespace" (hiccup/html (namespace-view/namespace' data)))]
    (-> html
        (fix-links version-key is-url-version)
        fix-web-links
        (fix-docs-links docs-domain version-key is-url-version)
        (fix-api-links version-key is-url-version)
        (fix-pg-links playground-domain version-key is-url-version))))
