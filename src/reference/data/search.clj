(ns reference.data.search
  (:require [reference.components.jdbc :refer [query one insert! exec]]
            [honeysql.helpers :refer :all]
            [clojure.java.jdbc :as clj-jdbc]))

(defmethod honeysql.format/fn-handler "ilike" [_ col qstr]
  (str (honeysql.format/to-sql col) " ilike " (honeysql.format/to-sql qstr)))

(defn search [jdbc version-id q]
  (let [match (str "%" q "%")]
    (query jdbc (-> (select :type :name :full_name :link)
                    (modifiers :distinct)
                    (from :search_table)
                    (where [:and
                            [:= :version_id version-id]
                            [:or [:ilike :name match]
                             [:ilike :full_name match]
                             [:ilike :short_description match]
                             [:ilike :description match]
                             [:ilike :detailed match]]])))))

(defn refresh [jdbc]
  (clj-jdbc/execute! (:conn jdbc) ["REFRESH MATERIALIZED VIEW search_table"]))