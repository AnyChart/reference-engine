(ns reference.web.views.admin-page
  (:require [garden.core :as garden]
            [hiccup.page :as hiccup-page]))


(defn css []
  (garden/css
    {:pretty-print? false}
    [:body {:padding "10px"}]
    [:.version-label {:width   "200px"
                      :display "inline-block"
                      :padding "4px 10px"}]
    [:select.custom-select {:width "200px"}]
    [:.main {:width "580px"}]
    [:.btn-secondary :.btn-danger :.btn-primary :.btn-group {:margin-left "10px"}]))


(defn page [versions]
  ;(println versions)
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

      [:p                                                   ;.alert.alert-primary {:role "alert"}
       [:p "Select a version and choose an action."]
       [:div.form-group
        [:select.custom-select {:id "versionSelect"}
         (for [version versions]
           [:option {:value (:key version)} (:key version)])]

        [:a.btn.btn-secondary {:role  "button"
                               :href  "/_admin_"
                               :title "Update page"
                               :rel   "nofollow"} [:i.fas.fa-sync-alt]]

        [:button.btn.btn-danger {:id    "deleteButton"
                                 :type  "button"
                                 :title "Remove version"} "Remove"]

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

        [:button.btn.btn-link {:id "showReportLink" :type "button"} "index.d.ts"]]

       ]]

     ]))