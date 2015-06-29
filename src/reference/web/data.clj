(ns reference.web.data
  (:require [selmer.parser :refer [render-file add-tag!]]
            [selmer.filters :refer [add-filter!]]
            [taoensso.timbre :as timbre :refer [info]]
            [reference.web.tree :refer [tree-view]]))

(add-tag! :link (fn [args context-map]
                  (let [path (clojure.string/split (first args) #"\.")
                        val (get-in context-map (map keyword path))
                        version (:version context-map)]
                    (str "/" version "/" val))))

(add-tag! :link-or-text (fn [args context-map]
                          (let [path (clojure.string/split (first args) #"\.")
                                val (get-in context-map (map keyword path))
                                version (:version context-map)]
                            (if (.startsWith val "anychart")
                              (str "<a href='/"
                                   version "/" val "'>" val "</a>")
                              val))))

(add-tag! :type-link (fn [args context-map]
                       (let [path (clojure.string/split (first args) #"\.")
                             val (get-in context-map (map keyword path))
                             version (:version context-map)]
                         (if (.startsWith val "anychart")
                           (str "<a class='code-style' href='/"
                                version "/" val "'>" val "</a>")
                           (str "<span class='code-style'>" val "</span>")))))

(add-tag! :playground (fn [args context-map content]
                        (let [val (get-in content [:playground :content])
                              version (:version context-map)
                              playground (:playground context-map)]
                          (str playground "/api/"
                               version (clojure.string/trim val) "-plain")))
          :endplayground)

(add-tag! :listings-and-samples (fn [args context-map]
                                  (let [entry (get context-map (keyword (first args)))]
                                    (render-file "templates/entries/samples.selmer"
                                                 {:entry entry
                                                  :version (:version context-map)
                                                  :playground (:playground context-map)}))))

(add-tag! :tree-view (fn [args context-map]
                       (let [entries (get context-map (keyword (first args)))]
                         (reduce str (map #(tree-view % (:version context-map)) entries)))))

(defn- fix-version [version data]
  (clojure.string/replace data "__VERSION__" version))

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

(defn- render-template [docs-domain playground-domain version template entry]
  (fix-links docs-domain
             version
             (render-file template
                          {:main entry
                           :version version
                           :playground playground-domain})))

(defn render-entry [docs-domain playground-domain version entry-type entry]
  (render-template docs-domain playground-domain version
                   (str "templates/entries/" entry-type ".selmer")
                   entry))
