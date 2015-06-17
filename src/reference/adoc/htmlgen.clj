(ns reference.adoc.htmlgen
  (:require [clostache.parser :refer [render]]
            [taoensso.timbre :as timbre :refer [info]]
            [clojure.java.io :refer [resource]]
            [reference.data.pages :as pages]))

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

(defn- render-namespace [docs-domain playground-domain version entry]
  (info "render-namespace" version (:full-name entry))
  (render-template docs-domain
                   playground-domain
                   version
                   ns-template
                   (assoc entry
                     :has-classes (not (empty? (:classes entry)))
                     :has-typedefs (not (empty? (:typedefs entry)))
                     :has-enums (not (empty? (:enums entry)))
                     :has-constants (not (empty? (:constants entry)))
                     :has-functions (not (empty? (:functions entry))))))

(defn- render-class [docs-domain playground-domain version entry]
  (info "render-class" version (:full-name entry))
  (render-template docs-domain
                   playground-domain
                   version
                   class-template
                   (assoc entry
                     :has-methods (not (empty? (:methods entry)))
                     :has-inherited-methods (not (empty? (:inherited-methods entry))))))

(defn- render-enum [docs-domain playground-domain version entry]
  (info "render-enum" version (:full-name entry))
  (render-template version
                   enum-template
                   (assoc entry
                          :has-fields (not (empty? (:fields entry))))))

(defn- render-typedef [docs-domain playground-domain version entry]
  (info "render-typedef" version (:full-name entry))
  (render-template docs-domain
                   playground-domain
                   version
                   typedef-template
                   entry))

(defn pre-render-top-level [docs-domain playground-domain jdbc version-id version-key top-level]
  (info "pre-render-top-level" version-key (count top-level))
  
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "class"
                                (:full-name %)
                                (render-class docs-domain playground-domain version-key %))
               (:classes top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "namespace"
                                (:full-name %)
                                (render-namespace docs-domain playground-domain version-key %))
               (:namespaces top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "typedef"
                                (:full-name %)
                                (render-typedef docs-domain playground-domain version-key %))
               (:typedefs top-level)))
  (doall (pmap #(pages/add-page jdbc
                                version-id
                                "enum"
                                (:full-name %)
                                (render-enum docs-domain playground-domain version-key %))
               (:enums top-level))))
