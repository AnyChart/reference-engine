(ns reference.components.jdbc
  (:require [clojure.java.jdbc :as clj-jdbc]
            [com.stuartsierra.component :as component]
            [honeysql.core :as sql]
            [honeysql.format :as sql-fmt]
            [taoensso.timbre :as timbre :refer [debug info error]])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(defn- connection-pool
  "Create a connection pool for the given database spec."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size
           test-connection-query
           idle-connection-test-period
           test-connection-on-checkin
           test-connection-on-checkout
           stringtype]
    :or   {excess-timeout              (* 30 60)
           idle-timeout                (* 3 60 60)
           minimum-pool-size           3
           maximum-pool-size           15
           test-connection-query       nil
           idle-connection-test-period 0
           test-connection-on-checkin  false
           test-connection-on-checkout false}}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                 (.setUser user)
                 (.setPassword password)
                 (.setMaxIdleTimeExcessConnections excess-timeout)
                 (.setMaxIdleTime idle-timeout)
                 (.setMinPoolSize minimum-pool-size)
                 (.setMaxPoolSize maximum-pool-size)
                 (.setIdleConnectionTestPeriod idle-connection-test-period)
                 (.setTestConnectionOnCheckin test-connection-on-checkin)
                 (.setTestConnectionOnCheckout test-connection-on-checkout)
                 (.setPreferredTestQuery test-connection-query))})

(defrecord JDBC [config conn]
  component/Lifecycle
  (start [this]
    (debug "DEBUG: Starting JDBC component with config:" config)
    (if conn
      (do
        (debug "DEBUG: JDBC connection pool already exists")
        this)
      (do
        (debug "DEBUG: Creating new JDBC connection pool")
        (try
          (let [connection (connection-pool config)]
            (debug "DEBUG: JDBC connection pool created successfully")
            (assoc this :conn connection))
          (catch Exception e
            (error e "Failed to create JDBC connection pool")
            (debug "DEBUG: JDBC connection error:" (.getMessage e))
            (throw e))))))
  (stop [this]
    (debug "DEBUG: Stopping JDBC component")
    (if-not conn
      (do
        (debug "DEBUG: No JDBC connection pool to close")
        this)
      (do
        (debug "DEBUG: Closing JDBC connection pool")
        (-> conn :datasource (.close))
        this))))

(defn new-jdbc [config]
  (map->JDBC {:config config}))

(defn sql [q]
  (debug "DEBUG: Formatting SQL query:" q)
  (sql/format q :quoting :ansi))

(defn query [jdbc q]
  (debug "DEBUG: Executing SQL query:" q)
  (info "DEBUG_DATA: DB query called with:" q)
  (try
    (if-not (:conn jdbc)
      (do
        (error "No JDBC connection available!")
        (info "DEBUG_DATA: JDBC connection is nil!")
        [])
      (let [sql-query (sql q)
            _ (info "DEBUG_DATA: Formatted SQL:" sql-query)
            result (clj-jdbc/query (:conn jdbc) sql-query)]
        (debug "DEBUG: Query returned" (count result) "rows")
        (info "DEBUG_DATA: Query result count:" (count result))
        (when (seq result)
          (info "DEBUG_DATA: Result tree size:" (count (:tree (first result)))))
        result))
    (catch Exception e
      (error e "SQL query error")
      (debug "DEBUG: Query failed:" (.getMessage e))
      (info "DEBUG_DATA: Query failed with exception:" (.getMessage e))
      (throw e))))

(defn one [jdbc q]
  (debug "DEBUG: Executing SQL query for one result:" q)
  (info "DEBUG_DATA: DB one function called with:" q)
  (let [result (first (query jdbc q))]
    (info "DEBUG_DATA: DB one function result exists:" (boolean result))
    result))

(defn exec [jdbc q]
  (debug "DEBUG: Executing SQL update:" q)
  (try
    (let [result (clj-jdbc/execute! (:conn jdbc) (sql q))]
      (debug "DEBUG: Update affected" (first result) "rows")
      result)
    (catch Exception e
      (error e "SQL update error")
      (debug "DEBUG: Update failed:" (.getMessage e))
      (throw e))))

(defn insert! [jdbc table data]
  (debug "DEBUG: Inserting into table:" table "data:" data)
  (try
    (let [result (clj-jdbc/insert! (:conn jdbc) table data)]
      (debug "DEBUG: Insert successful, result:" result)
      result)
    (catch Exception e
      (error e "SQL insert error")
      (debug "DEBUG: Insert failed:" (.getMessage e))
      (throw e))))

(defn insert-multiple! [jdbc table data]
  (debug "DEBUG: Inserting multiple rows into table:" table "count:" (count data))
  (if (seq data)
    (try
      (let [result (apply clj-jdbc/insert! (concat [(:conn jdbc) table] data))]
        (debug "DEBUG: Multiple insert successful")
        result)
      (catch Exception e
        (error e "SQL multiple insert error")
        (debug "DEBUG: Multiple insert failed:" (.getMessage e))
        (throw e)))))
