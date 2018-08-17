(ns reference.web.views.resources
  (:require [selmer.parser :as selmer-parser]
            [cheshire.core :as json]))


(defn minimize-tree [tree]
  (cond
    (map? tree) (concat
                  [(:name tree) (first (:kind tree))]
                  (when (seq (:children tree))
                    (map minimize-tree (:children tree))))
    (sequential? tree) (map minimize-tree tree)))


(defn update-tree [data]
  (update data :tree (fn [tree]
                       (let [tree (json/parse-string tree true)
                             tree (minimize-tree tree)]
                         (json/generate-string tree)))))

(defn init-script [data]
  (selmer-parser/render-file "templates/init-script.selmer" data))

(defn init-script-fast [data]
  (selmer-parser/render-file "templates/init-script-fast.selmer" data))


(defn google-tag-manager []
  (str "<!-- Google Tag Manager -->
<noscript><iframe src=\"//www.googletagmanager.com/ns.html?id=GTM-5B8NXZ\"
height=\"0\" width=\"0\" style=\"display:none;visibility:hidden\"></iframe></noscript>
<script>(function(w,d,s,l,i){w[l]=w[l]||[];w[l].push({'gtm.start':
new Date().getTime(),event:'gtm.js'});var f=d.getElementsByTagName(s)[0],
j=d.createElement(s),dl=l!='dataLayer'?'&l='+l:'';j.async=true;j.src=
'//www.googletagmanager.com/gtm.js?id='+i+dl;f.parentNode.insertBefore(j,f);
})(window,document,'script','dataLayer','GTM-5B8NXZ');</script>
<!-- End Google Tag Manager -->"))