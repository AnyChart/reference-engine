(ns reference.adoc.htmlgen
  (:require [clostache.parser :refer [render]]
            [reference.config :as config]
            [clojure.java.io :refer [file resource]]
            [clojure.java.shell :refer [sh]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn- fix-links [version data]
  (clojure.string/replace data
                          #"\{@link ([^}]+)\}"
                          (str "<a class='type-link' href='/" version "/$1'>$1</a>")))

(def ns-template (slurp (resource "templates/ns.mustache")))
(def class-template (slurp (resource "templates/class.mustache")))
(def enum-template (slurp (resource "templates/enum.mustache")))
(def typedef-template (slurp (resource "templates/typedef.mustache")))
(def fn-template (slurp (resource "templates/fn.mustache")))
(def const-template (slurp (resource "templates/const.mustache")))
(def method-template (slurp (resource "templates/method.mustache")))
(def example-template (slurp (resource "templates/example.mustache")))
  
(defn- render-template [version template entry]
  (fix-links version
             (render template
                     {:main entry
                      :link #(str "/" version "/" %)
                      :type-link (fn [text]
                                   (fn [render-fn]
                                     (let [type (render-fn text)]
                                       (if (.startsWith type "anychart")
                                         (str "<a class='type-link' href='/"
                                              version "/" type "'>" type "</a>")
                                         type))))}
                     {:fn-part fn-template
                      :method-part method-template
                      :const-part const-template
                      :examples example-template})))

(defn- render-namespace [version entry]
  (info "render-namespace" version (:full-name entry))
  (render-template version
                   ns-template
                   (assoc entry
                     :has-classes (not (empty? (:classes entry)))
                     :has-typedefs (not (empty? (:typedefs entry)))
                     :has-enums (not (empty? (:enums entry)))
                     :has-constants (not (empty? (:constants entry)))
                     :has-functions (not (empty? (:functions entry))))))

(defn- render-class [version entry]
  (info "render-class" version (:full-name entry))
  (render-template version
                   class-template
                   (assoc entry
                     :has-methods (not (empty? (:methods entry)))
                     :has-inherited-methods (not (empty? (:inherited-methods entry))))))

(defn- render-enum [version entry]
  (info "render-enum" version (:full-name entry))
  (render-template version
                   enum-template
                   (assoc entry
                          :has-fields (not (empty? (:fields entry))))))

(defn- render-typedef [version entry]
  (info "render-typedef" version (:full-name entry))
  (render-template version
                   typedef-template
                   entry))

(defn- save [version entry data]
  (if data
    (spit (str config/data-path "/versions-data/" version "/" (:full-name entry) ".html") data)))

(defn pre-render-top-level [version top-level]
  (info "pre-render-top-level" version (count top-level))
  (let [path (str config/data-path "versions-data/" version)]
    (if (.exists (file path))
      (sh "rm" "-rf" path))
    (sh "mkdir" path)
    (info "rendering into" path))
  (doall (pmap #(save version % (render-class version %)) (:classes top-level)))
  (doall (pmap #(save version % (render-namespace version %)) (:namespaces top-level)))
  (doall (pmap #(save version % (render-typedef version %)) (:typedefs top-level)))
  (doall (pmap #(save version % (render-enum version %)) (:enums top-level))))
