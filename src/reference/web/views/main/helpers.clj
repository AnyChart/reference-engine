(ns reference.web.views.main.helpers
  (:require [hiccup.core :as hiccup]
            [clojure.string :as string]))


(defn edit-link []
  ;(str "https://github.com/AnyChart/api-reference/edit/" version (-> context-map :main :file))
  "https://github.com/AnyChart/api.anychart.com")


(defn link [{:keys [version is-url-version playground]} val]
  (if (.contains val "://")
    val
    (str (when is-url-version (str "/" version)) "/" val)))


(defn playground-link [{:keys [version is-url-version playground]} val]
  (str "//" playground "/api"
       (when is-url-version (str "/" version))
       (string/trim val) "-plain"))


(defn type-link [{:keys [is-url-version version]} type]
  (let [replaced-links (string/replace type
                                       #"anychart[\w\.]+"
                                       #(str "<a class='type-link code-style' href='/"
                                             (when is-url-version
                                               (str version "/"))
                                             %1 "'>" %1 "</a>"))]
    (str "<span class='code-style'>" replaced-links "</span>")))


(defn link-or-text [{:keys [is-url-version version]} type]
  (string/replace type
                  #"anychart[\w\.]+"
                  #(str "<a class='type-link' href='/"
                        (when is-url-version
                          (str version "/"))
                        %1 "'>" %1 "</a>")))


(defn is-override-expand [method last counter]
  (let [default-override-index (:default-override-index method)]
    (if (and default-override-index)
      (when (= default-override-index counter)
        "in")
      (when last
        "in"))))


(defn compound-type [data types]
  (->> types
       (map #(link-or-text data (hiccup/h %)))
       (string/join " | ")))


(defn table-style [text]
  (string/replace text #"<table>" "<table class='table table-condensed'>"))


(defn samples [{:keys [main version is-url-version show-samples playground] :as data} entry]
  (when (and show-samples (:has-playground-samples entry))
    [:div.small-group
     [:p
      [:strong "Try it:"]]
     [:ul.list.list-dotted
      (for [s (:playground-samples entry)]
        [:li
         [:a.code-style {:target "_blank"
                         :href   (playground-link data (:file s))}
          (:title s)]])]]))


(defn listings [{:keys [main version is-url-version show-samples playground] :as data} entry]
  (when (:has-listings entry)
    (for [l (:listings entry)]
      [:div.small-group
       [:div.collapse-group
        [:a
         {:href        (str "#listing-" (:id l))
          :data-toggle "collapse"}
         [:i.ac.ac-andrews-pitchfork]
         " " (:title l)]
        [:div.collapse.collapse-div {:id (str "listing-" (:id l))}
         [:div.collapse-content
          [:pre.prettyprint.linenums:1 (:code l)]]]]])))


(defn listing-and-samples [data entry]
  (list
    (listings data entry)
    (samples data entry)))