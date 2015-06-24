(ns reference.adoc.categories)

(defn- get-method-category [method]
  (first (reduce (fn [res override]
                   (if (:has-category override)
                     (conj res (:category override))
                     res)) [] (:overrides method))))

(defn- get-categories [methods]
  (reduce (fn [res method]
            (let [categories (filter some? (map get-method-category method))]
              (concat res categories)))
          [] methods))

(defn- get-category-methods [methods category]
  )

(defn- build-class-categories [class]
  (let [categories (get-categories (:methods class))]
    (assoc class
           :categories categories
           :has-categories (boolean (seq categories)))))
