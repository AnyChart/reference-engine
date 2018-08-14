(ns reference.web.views.common
  (:require [hiccup.core :as hiccup]
            [clojure.string :as string]))


(defn edit-link []
  "https://github.com/AnyChart/api.anychart.com")


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
         {:href        (str "#listing" (:id l))
          :data-toggle "collapse"}
         [:i.ac.ac-andrews-pitchfork]
         (:title l)]
        [:div.collapse.collapse-div {:id (str "listing-" (:id l))}
         [:div.collapse-content
          [:pre.prettyprint.linenums:1 (:code l)]]]]])))


(defn listing-and-samples [data]
  (list
    (listings data (:main data))
    (samples data (:main data))))