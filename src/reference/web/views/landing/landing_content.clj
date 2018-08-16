(ns reference.web.views.landing.landing-content)


(defn landing-data [data]
  [:div.landing-page

   [:h1 "API Reference"]

   [:p.important
    "AnyChart API Reference is a place where you can find detailed description of each namespace, class, method or property, along with ready-to-try the samples."]

   [:div.products-list

    [:a.item.product.AnyChart.active {:href "//anychart.com/products/anychart/"}
     [:span.product-icon [:i.ac.ac-anychart.ac-product]]
     [:span.product-title "AnyChart"]]

    [:a.item.product.AnyStock.active {:href "//anychart.com/products/anystock/"}
     [:span.product-icon [:i.ac.ac-anystock.ac-product]]
     [:span.product-title "AnyStock"]]

    [:a.item.product.AnyMap.active {:href "//anychart.com/products/anymap/"}
     [:span.product-icon [:i.ac.ac-anymap.ac-product]]
     [:span.product-title "AnyMap"]]

    [:a.item.product.AnyGantt.active {:href "//anychart.com/products/anygantt/"}
     [:span.product-icon [:i.ac.ac-anygantt.ac-product]]
     [:span.product-title "AnyGantt"]]]

   [:h2.bottom-line "API Reference Features"]

   [:div.feature
    [:img.img-in-context {:src "/i/method_overload.png"}]
    [:h4 "Methods Overloading"]
    [:p "We are trying to reduce the amount of code necessary for charts creation as much as possible. In order to achieve this, each method may accept different set of parameters which change its functionality, and this is called "
     [:em "method overloading"]
     ". You can find list of overloads each method has, with all its parameters in the head section of each method description. We provide at least one Live Sample for each overload, this sample can be launched it in the "
     [:a {:href "//playground.anychart.com"} "AnyChart Playground"]
     " and you will instantly see what it does."]
    [:div.clearfix]]

   [:div.feature
    [:img.img-in-context {:src "/i/playground_preview.jpg"}]
    [:h4 "Live samples"]
    [:p "AnyChart has thousands of methods, namespaces, enums and properties. To ease the process of understanding and give you a clear perception them, we provide at least one "
     [:em "Live Sample"]
     " for each function, method or property in "
     [:a {:href "//playground.anychart.com"} "AnyChart Playground"]
     ". Yes, creating so many samples takes a lot of time and requires thoroughness, but we strive to be the best and make your learning curve less steep, and we believe that the ability to try everything with your own hands is one of the most potent ways of learning."]
    [:div.clearfix]]

   [:div.feature
    [:img.img-in-context {:src "/i/search_preview.jpg"}]
    [:h4 "Search"]
    [:p "AnyChart API Search helps you to find everything you need as fast as possible. The search results are grouped in categories, functions and methods come first, then enums, typedefs and then classes follow. Some names are really common ('fill' is a good example of that), we added a number of repetitions in a search result, click on this number to see the full list of methods matching your query."]
    [:div.clearfix]]

   [:h2.bottom-line "How to contribute"]

   [:p "We hope API Reference helps you in your development process. If you want to help us back: please report errors, typos and suggest better samples, to do so either fork and create a pull request or create an issue in "
    [:a {:target "_blank",
         :href   "https://github.com/AnyChart/api-reference"}
     "AnyChart API repository on GitHub"]
    ". We will be very happy to hear out everything you have to say and consider your suggestions."]])