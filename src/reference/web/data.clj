(ns reference.web.data
  (:require [selmer.parser :refer [render-file add-tag! render]]
            [selmer.filters :refer [add-filter!]]
            [taoensso.timbre :as timbre :refer [info]]
            [reference.web.tree :refer [tree-view]]))

(defn- escape-str [str]
  (render "{{val}}" {:val str}))

(add-tag! :link (fn [args context-map]
                  (let [path (clojure.string/split (first args) #"\.")
                        val (get-in context-map (map keyword path))
                        version (:version context-map)]
                    (if (.contains val "://")
                      val
                      (str "/" version "/" val)))))

(add-tag! :edit-link (fn [args context-map]
                       (let [version (:version context-map)
                             path (-> context-map :main :file)]
                         ;(str "https://github.com/AnyChart/api-reference/edit/" version path)
                         "https://github.com/AnyChart/api.anychart.com")))

(add-tag! :link-or-text (fn [args context-map]
                          (let [path (clojure.string/split (first args) #"\.")
                                val (escape-str (get-in context-map (map keyword path)))
                                version (:version context-map)
                                replaced-links (clojure.string/replace val
                                                                       #"anychart[\w\.]+"
                                                                       #(str "<a class='type-link' href='/"
                                                                             version "/" %1 "'>" %1 "</a>"))]
                            replaced-links)))

(add-tag! :type-link (fn [args context-map]
                       (let [path (clojure.string/split (first args) #"\.")
                             val (escape-str (get-in context-map (map keyword path)))
                             version (:version context-map)
                             replaced-links (clojure.string/replace val
                                                                    #"anychart[\w\.]+"
                                                                    #(str "<a class='type-link code-style' href='/"
                                                                          version "/" %1 "'>" %1 "</a>"))]
                         (str "<span class='code-style'>" replaced-links "</span>"))))

(add-tag! :playground (fn [args context-map content]
                        (let [val (get-in content [:playground :content])
                              version (:version context-map)
                              playground (:playground context-map)]
                          (str "//" playground "/api/"
                               version (clojure.string/trim val) "-plain")))
          :endplayground)

(add-tag! :is-override-expand (fn [args context-map]
                       (let [paths (map #(clojure.string/split % #"\.") args)
                             vals (map #(get-in context-map (map keyword %)) paths)
                             method (first vals)
                             default-override-index (:default-override-index method)
                             last (second vals)
                             counter (nth vals 2)]
                         (if (and default-override-index)
                           (when (= (inc default-override-index) counter)
                             "in")
                           (when last
                             "in")))))

(add-tag! :listings-and-samples (fn [args context-map]
                                  (let [entry (get context-map (keyword (first args)))]
                                    (render-file "templates/entries/samples.selmer"
                                                 {:entry entry
                                                  :show-samples (:show-samples context-map)
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

(defn- fix-web-links [docs-domain version data]
  (fix-docs-links docs-domain
                  version
                  (clojure.string/replace data
                                          #"\{@link ([^\}]+)\}"
                                          (str "<a class='type-link' href='$1'>$1</a>"))))

(defn- fix-links [docs-domain version data]
  (fix-web-links docs-domain
                 version
                 (clojure.string/replace data
                                         #"\{@link (anychart[^\}]+)\}"
                                         (str "<a class='type-link' href='/" version "/$1'>$1</a>"))))

(defn- render-template [docs-domain playground-domain version-key show-samples template entry]
  (fix-links docs-domain
             version-key
             (render-file template
                          {:main entry
                           :version version-key
                           :show-samples true
                           :playground playground-domain})))

(defn render-entry [docs-domain playground-domain version-key show-samples entry-type entry]
  (render-template docs-domain playground-domain version-key show-samples
                   (str "templates/entries/" entry-type ".selmer")
                   entry))
