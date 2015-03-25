(ns app.page
  (:require [goog.dom]
            [goog.array]
            [goog.style]
            [goog.events]
            [goog.events.EventType]
            [goog.dom.classes]
            [clojure.string]))

(enable-console-print!)

(defn- nodelist-to-seq [nl]
  (if nl
    (let [result-seq (map #(aget nl %) (range (.-length nl)))]
      (doall result-seq))))

(defn- first-child [tag container]
  (aget (goog.dom.getElementsByTagNameAndClass tag nil container) 0))

(defn- tag-by-name-and-class [tag class el]
  (goog.dom.getElementsByTagNameAndClass tag class el))

(defn init-method [el]
  (let [tabs-header (aget (goog.dom.getElementsByTagNameAndClass "ul" "overrides" el) 0)
        tabs (goog.dom.getElementsByTagNameAndClass "li" nil tabs-header)
        tabs-content (aget (goog.dom.getElementsByTagNameAndClass "div" "overrides" el) 0)
        divs (goog.dom.getChildren tabs-content)]
    (goog.dom.classes/add (first-child "li" tabs-header) "active")
    (goog.dom.classes/add (first-child "div" tabs-content) "active")
    (doall (map (fn [li]
           (goog.events.listen
            (first-child "a" li)
            goog.events.EventType.CLICK
            (fn [e]
              (.preventDefault e)
              (let [index (.call Array.prototype.indexOf tabs li)]
                (goog.dom.classes/remove (aget
                                          (tag-by-name-and-class "li" "active" tabs-header)
                                          0) "active")
                (goog.dom.classes/remove (aget
                                          (tag-by-name-and-class "div" "active" tabs-content)
                                          0) "active")
                (goog.dom.classes/add (aget tabs index) "active")
                (goog.dom.classes/add (aget divs index) "active")))))
         (nodelist-to-seq tabs)))))

(defn- init-methods []
  (map init-method (nodelist-to-seq
                    (goog.dom.getElementsByTagNameAndClass "div" "method"))))

(defn- extract-namespace [page]
  (clojure.string/replace page #"\.[^\.]+$" ""))

(defn- extract-name [page]
  (last (re-matches #".+\.([^\.]+)$" page)))

(defn- init-path [version page info]
  (let [ns-el (goog.dom.getElement "path-ns")
        ns-link (first-child "a" ns-el)
        ns-span (first-child "span" ns-el)
        class-el (goog.dom.getElement "path-class")
        class-link (first-child "a" class-el)
        ns (if (= (:kind info) "namespace")
             page
             (extract-namespace page))
        class (if-not (= (:kind info) "namespace")
                (extract-name page))]
    (if-not (= (:kind info) "namespace")
      (do
        (goog.style/showElement class-el true)
        (goog.style/showElement ns-span true)
        (set! (.-innerHTML class-link) class)
        (.setAttribute class-link "href" page))
      (do
        (goog.style/showElement class-el false)
        (goog.style/showElement ns-span false)))
    (.setAttribute ns-link "href" (str "/" version "/" ns))
    (set! (.-innerHTML ns-link) ns)))

(defn- create-editor [el]
  (let [editor (.edit js/ace el)]
    (.setTheme editor "ace/theme/tomorrow")
    (.setOptions editor #js {:maxLines 999999})
    (.setReadOnly editor true)
    (.setMode (.getSession editor) "ace/mode/javascript")))

(defn- close-listing [code e]
  (.stopPropagation e)
  (goog.style.showElement code false))

(defn- init-concrete-listing [code]
  (let [close (first-child "a" code)
        src (aget (goog.dom.getElementsByTagNameAndClass "div" nil code) 0)]
    (create-editor src)
    (goog.events.listen close goog.events.EventType.CLICK #(close-listing code %))
    (.log js/console (str "init" src))))

(defn- init-listing-link [link]
  (let [target-listing (.substring (.getAttribute link "href") 1)]
    (goog.events.listen link
                        goog.events.EventType.CLICK
                        (fn [e]
                          (.preventDefault e)
                          (goog.style.showElement (goog.dom.getElement target-listing)
                                                  true)))))

(defn- init-listing [listing]
  (let [links (goog.dom.getElementsByTagNameAndClass "a" "btn" listing)
        listings (goog.dom.getElementsByTagNameAndClass "div" "code-listing" listing)]
    (goog.array.map links init-listing-link)
    (goog.array.map listings init-concrete-listing)))

(defn- init-listings []
  (let [listings (goog.dom.getElementsByTagNameAndClass "div" "listings")]
    (goog.array.map listings init-listing)))

(init-listings)

(defn init [version page info]
  (doall (init-methods))
  (doall (init-listings))
  (init-path version page (if-not (contains? info :kind)
                            (assoc info :kind (get info "kind"))
                            info)))
