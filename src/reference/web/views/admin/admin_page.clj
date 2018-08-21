(ns reference.web.views.admin.admin-page
  (:require [garden.core :as garden]
            [hiccup.page :as hiccup-page]
            [reference.web.views.resources :as resources]
            [reference.web.views.common :as common]))


;; deprecated
(defn css []
  (garden/css
    {:pretty-print? false}
    [:body {:padding "10px"}]
    [:.version-label {:width   "200px"
                      :display "inline-block"
                      :padding "4px 10px"}]
    [:select.custom-select {:width "200px"}]
    [:.main {:width "700px"}]
    [:select :.btn-secondary :.btn-danger :.btn-group :.btn-link {:margin-right "10px"}]))


;; deprecated
(defn page-bootstrap-4 [versions]
  (hiccup-page/html5
    {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name    "viewport"
             :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
     [:link {:crossorigin "anonymous"
             :integrity   "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
             :href        "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
             :rel         "stylesheet"}]
     [:link {:crossorigin "anonymous"
             :integrity   "sha384-O8whS3fhG2OnA5Kas0Y9l3cfpmYjapjI0E4theH4iuMD+pLhbf6JI0jIMfYcK3yZ"
             :href        "https://use.fontawesome.com/releases/v5.1.1/css/all.css",
             :rel         "stylesheet"}]
     [:style (css)]]

    [:body
     [:script {:src "/admin/main.js"}]
     [:script {:src "https://code.jquery.com/jquery-3.2.1.min.js"}]
     [:script {:crossorigin "anonymous"
               :integrity   "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
               :src         "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"}]
     [:script {:crossorigin "anonymous"
               :integrity   "sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
               :src         "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"}]

     [:h5 "Version management"]

     [:div.main
      [:div.alert.alert-primary {:role "alert"}
       "To start update versions, just push this button." [:br]
       "This action is used by github webhook."
       [:div [:a.btn.btn-success {:role "button"
                                  :href "/_update_reference_"
                                  :rel  "nofollow"} "Start updating versions"]]]

      [:p
       [:p "Select a version and choose an action."]
       [:div.form-group

        [:a.btn.btn-secondary {:id    "refreshButton"
                               :role  "button"
                               :href  "/_admin_"
                               :title "Refresh page"
                               :rel   "nofollow"} [:i.fas.fa-sync-alt]]

        [:select.custom-select {:id "versionSelect"}
         (for [version versions]
           [:option {:value (:key version)} (:key version)])]

        [:div.btn-group
         [:button.btn.btn-primary.dropdown-toggle
          {:id            "dropdownMenuButton"
           :type          "button"
           :aria-expanded "false"
           :aria-haspopup "true"
           :data-toggle   "dropdown"
           :title         "Rebuild version"}
          "Rebuild"]
         [:div.dropdown-menu {:aria-labelledby "dropdownMenuButton"}
          [:a.dropdown-item {:id    "rebuildCommit"
                             :href  "#"
                             :title "Rebuild according to commit message flags"
                             :rel   "nofollow"}
           "commit message flags"]
          [:a.dropdown-item {:id    "rebuildFast"
                             :href  "#"
                             :title "Rebuild without link checking"
                             :rel   "nofollow"}
           "fast"]
          [:a.dropdown-item {:id    "rebuildFull"
                             :href  "#"
                             :title "Rebuild with d.ts generation"
                             :rel   "nofollow"}
           "with d.ts"]]]

        [:button.btn.btn-danger {:id    "deleteButton"
                                 :type  "button"
                                 :title "Remove version"} "Remove"]

        [:button.btn.btn-link {:id "indexLink" :type "button"} "index.d.ts"]
        [:button.btn.btn-link {:id "graphicsLink" :type "button"} "graphics.d.ts"]]]]]))




(defn body [{:keys [commit] :as data} versions]
  [:body
   [:script {:src "/admin/main.js"}]
   (common/styles commit)
   [:div#ac-header
    (common/brand)
    [:div.pull-right.helpers
     (common/support)]]

   [:div.wrapper.container-fluid
    [:div.row
     [:div.col-md-12
      [:div#article-content

       [:div.admin-panel
        [:h4 "Version management"]

        [:div..update-versions-box.row
         [:div.col-sm-8 [:div.text "To start update versions, just push this button." [:br]
                         "This action is used by GitHub webhook."]]
         [:div.col-sm-4 [:a#updateVersionsButton.btn.btn-default.btn-success {:role "button"
                                                                              :type "button"
                                                                              :href "/_update_reference_"
                                                                              :rel  "nofollow"} "Start updating versions"]]]

        [:p "Select a version and choose an action."]
        [:form.form-inline

         [:a#refreshButton.btn.btn-secondary {:role  "button"
                                              :href  "/_admin_"
                                              :title "Refresh page"
                                              :rel   "nofollow"}
          [:i.glyphicon.glyphicon-refresh]
          ;[:i.fas.fa-sync-alt]
          ]

         [:select.form-control.custom-select {:id "versionSelect"}
          (for [version versions]
            [:option {:value (:key version)} (:key version)])]

         [:div.btn-group
          [:button.btn.btn-primary.dropdown-toggle
           {:id            "dropdownMenuButton"
            :type          "button"
            :aria-expanded "false"
            :aria-haspopup "true"
            :data-toggle   "dropdown"
            :title         "Rebuild version"}
           "Rebuild "
           [:span.caret]]
          [:ul.dropdown-menu
           [:li [:a {:id    "rebuildCommit"
                     :href  "#"
                     :title "Rebuild according to commit message flags"
                     :rel   "nofollow"}
                 "Commit message flags"]]
           [:li [:a {:id    "rebuildFast"
                     :href  "#"
                     :title "Rebuild without link checking"
                     :rel   "nofollow"}
                 "Fast"]]
           [:li [:a {:id    "rebuildFull"
                     :href  "#"
                     :title "Rebuild with link checking"
                     :rel   "nofollow"}
                 "With d.ts"]]]]

         [:button.btn.btn-danger {:id    "deleteButton"
                                  :type  "button"
                                  :title "Remove version"} "Remove"]

         [:button.btn.btn-link {:id "indexLink" :type "button"} "index.d.ts"]
         [:button.btn.btn-link {:id "graphicsLink" :type "button"} "graphics.d.ts"]]

        [:p.other-buttons-box
         [:a.btn.btn-link {:role "button"
                           :href "https://github.com/AnyChart/api.anychart.com"
                           :rel  "nofollow"}
          "GitHub API Reference"]
         [:a.btn.btn-link {:role "button"
                           :href "https://github.com/AnyChart/reference-engine"
                           :rel  "nofollow"}
          "GitHub API Reference Engine"]]]]]]]])


(defn page [data versions]
  (hiccup-page/html5
    {:lang "en"}
    (common/head data)
    (body data versions)))