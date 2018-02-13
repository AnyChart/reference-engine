(ns reference.web.data
  (:require [selmer.parser :refer [render-file add-tag! render]]
            [selmer.filters :refer [add-filter!]]
            [taoensso.timbre :as timbre :refer [info]]
            [cheshire.core :refer [parse-string]]
            [reference.web.tree :refer [tree-view-partial]]))


(defn- escape-str [str]
  (render "{{val}}" {:val str}))


(add-tag! :link (fn [args context-map]
                  (let [path (clojure.string/split (first args) #"\.")
                        val (get-in context-map (map keyword path))
                        version (:version context-map)
                        is-url-version (:is-url-version context-map)]
                    (if (.contains val "://")
                      val
                      (str (when is-url-version (str "/" version)) "/" val)))))


(add-tag! :edit-link (fn [args context-map]
                       (let [version (:version context-map)
                             path (-> context-map :main :file)]
                         ;(str "https://github.com/AnyChart/api-reference/edit/" version path)
                         "https://github.com/AnyChart/api.anychart.com")))


(add-tag! :link-or-text (fn [args context-map]
                          (let [path (clojure.string/split (first args) #"\.")
                                val (escape-str (get-in context-map (map keyword path)))
                                version (:version context-map)
                                is-url-version (:is-url-version context-map)
                                replaced-links (clojure.string/replace val
                                                                       #"anychart[\w\.]+"
                                                                       #(str "<a class='type-link' href='/"
                                                                             (when is-url-version
                                                                               (str version "/"))
                                                                             %1 "'>" %1 "</a>"))]
                            replaced-links)))


(add-tag! :type-link (fn [args context-map]
                       (let [path (clojure.string/split (first args) #"\.")
                             val (escape-str (get-in context-map (map keyword path)))
                             version (:version context-map)
                             is-url-version (:is-url-version context-map)
                             replaced-links (clojure.string/replace val
                                                                    #"anychart[\w\.]+"
                                                                    #(str "<a class='type-link code-style' href='/"
                                                                          (when is-url-version
                                                                            (str version "/"))
                                                                          %1 "'>" %1 "</a>"))]
                         (str "<span class='code-style'>" replaced-links "</span>"))))


(add-tag! :playground (fn [args context-map content]
                        (let [val (get-in content [:playground :content])
                              version (:version context-map)
                              is-url-version (:is-url-version context-map)
                              playground (:playground context-map)]
                          (str "//" playground "/api"
                               (when is-url-version (str "/" version))
                               (clojure.string/trim val) "-plain")))
          :endplayground)


(add-tag! :is-override-expand (fn [args context-map]
                                (let [paths (map #(clojure.string/split % #"\.") args)
                                      vals (map #(get-in context-map (map keyword %)) paths)
                                      ;; args
                                      method (first vals)
                                      last (second vals)
                                      counter (nth vals 2)

                                      default-override-index (:default-override-index method)]
                                  (if (and default-override-index)
                                    (when (= (inc default-override-index) counter)
                                      "in")
                                    (when last
                                      "in")))))


(add-tag! :listings-and-samples (fn [args context-map]
                                  (let [entry (get context-map (keyword (first args)))]
                                    (render-file "templates/entries/samples.selmer"
                                                 {:entry          entry
                                                  :show-samples   (:show-samples context-map)
                                                  :version        (:version context-map)
                                                  :is-url-version (:is-url-version context-map)
                                                  :playground     (:playground context-map)}))))


(add-tag! :tree-view (fn [args context-map]
                       (let [entries (parse-string (get context-map (keyword (first args))) true)
                             url (get context-map (keyword (second args)))]
                         (reduce str (map #(tree-view-partial % (:version context-map)
                                                              (:is-url-version context-map) url) entries)))))


(defn- fix-version [html version]
  (clojure.string/replace html "__VERSION__" version))


(defn- fix-docs-links [html docs-domain version is-url-version]
  (clojure.string/replace html
                          #"\{docs:([^\}]+)\}([^\{]+)\{docs\}"
                          (str "<a href='//" docs-domain
                               (when is-url-version (str "/" version))
                               "/$1'>$2</a>")))


(defn- fix-api-links [html version is-url-version]
  (clojure.string/replace html
                          #"\{api:([^\}]+)\}([^\{]+)\{api\}"
                          (str "<a href='"
                               (when is-url-version (str "/" version))
                               "/$1'>$2</a>")))


(defn- fix-pg-links [html playground-domain version is-url-version]
  (clojure.string/replace html
                          #"\{pg:([^\}]+)\}([^\{]+)\{pg\}"
                          (str "<a href='//" playground-domain "/api"
                               (when is-url-version (str "/" version))
                               "/$1'>$2</a>")))


(defn- fix-web-links [html]
  (clojure.string/replace html
                          #"\{@link ([^\}]+)\}"
                          (str "<a class='type-link' href='$1'>$1</a>")))


(defn- fix-links [html version is-url-version]
  (clojure.string/replace html
                          #"\{@link (anychart[^\}]+)\}"
                          (str "<a class='type-link' href='/"
                               (when is-url-version (str version "/"))
                               "$1'>$1</a>")))


(defn render-entry [docs-domain playground-domain version-key show-samples entry-type entry is-url-version]
  (let [template (str "templates/entries/" entry-type ".selmer")
        html (render-file template
                          {:main           entry
                           :version        version-key
                           :is-url-version is-url-version
                           :show-samples   true
                           :playground     playground-domain})]
    (-> html
        (fix-links version-key is-url-version)
        fix-web-links
        (fix-docs-links docs-domain version-key is-url-version)
        (fix-api-links version-key is-url-version)
        (fix-pg-links playground-domain version-key is-url-version))))
