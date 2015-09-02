(ns reference.data.seo)

(def entries
  ["AnyChart - HTML5 charts help you to understand any data"
   "<a href='http://wwww.anychart.com/products/anychart/overview/'>AnyChart</a> - JavaSript charts is a trusted solution for data visualization"
   "AnyChart - JavaSript charts for web and mobile"
   "AnyChart - The most comprehensive JavaScript charting library"
   "AnyChart - Create interactive HTML5 charts easily for your web projects"
   "IE 6? Latest Chrome? Mobile browsers? AnyChart just works everywhere!"
   "AnyStock's main purpose is to display financial JavaSript charts"
   "AnyMap is a robust interactive Javascript/HTML5 maps library"])

(defn random-entry []
  (let [idx (rand-int (-> entries count inc))]
    (if (= idx (count entries))
      nil
      (nth entries idx))))
