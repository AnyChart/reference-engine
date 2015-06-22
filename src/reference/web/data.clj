(ns reference.web.data
  (:require [clostache.parser :refer [render]]
            [clojure.java.io :refer [resource]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- fix-version [version data]
  (clojure.string/replace data
                          "__VERSION__"
                          version))

(defn- fix-docs-links [docs-domain version data]
  (clojure.string/replace (fix-version version data)
                          #"\{docs:([^\}]+)\}([^\{]+)\{docs\}"
                          (str "<a href='//" docs-domain "/" version "/$1'>$2</a>")))

(defn- fix-links [docs-domain version data]
  (fix-docs-links docs-domain
                  version
                  (clojure.string/replace data
                                          #"\{@link ([^}]+)\}"
                                          (str "<a class='type-link' href='/" version "/$1'>$1</a>"))))

(def ns-template (slurp (resource "templates/ns.mustache")))
(def class-template (slurp (resource "templates/class.mustache")))
(def enum-template (slurp (resource "templates/enum.mustache")))
(def typedef-template (slurp (resource "templates/typedef.mustache")))
(def fn-template (slurp (resource "templates/fn.mustache")))
(def const-template (slurp (resource "templates/const.mustache")))
(def method-template (slurp (resource "templates/method.mustache")))
(def example-template (slurp (resource "templates/samples.mustache")))
(def listing-template (slurp (resource "templates/listing.mustache")))

(defn- render-template [docs-domain playground-domain version template entry]
  (fix-links docs-domain
             version
             (render template
                     {:main entry
                      :link #(str "/" version "/" %)
                      :type-link (fn [text]
                                   (fn [render-fn]
                                     (let [type (render-fn text)]
                                       (if (.startsWith type "anychart")
                                         (str "<a class='type-link' href='/"
                                              version "/" type "'>" type "</a>")
                                         type))))
                      :playground-link (fn [text]
                                         (fn [render-fn]
                                           (str "//" playground-domain "/api/"
                                                version (render-fn text) "-plain")))}
                     {:fn-part fn-template
                      :method-part method-template
                      :const-part const-template
                      :listing-part listing-template
                      :samples-part example-template})))

(defn render-entry [docs-domain playground-domain version entry-type entry]
  (render-template docs-domain
                   playground-domain
                   version
                   (case entry-type
                     "namespace" ns-template
                     "class" class-template
                     "enum" enum-template
                     "typedef" typedef-template)
                   entry))
