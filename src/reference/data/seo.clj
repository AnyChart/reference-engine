(ns reference.data.seo)

(def entries
  ["AnyChart - HTML5 charts help you to understand any data"
   "AnyChart - JavaSript charts is a trusted solution for data visualization"
   "AnyStock main purpose is to display financial JavaSript charts"
   "AnyMap is a robust interactive Javascript/HTML5 maps library"])

(defn random-entry []
  (let [idx (rand-int (-> entries count inc))]
    (if (= idx (count entries))
      nil
      (nth entries idx))))
