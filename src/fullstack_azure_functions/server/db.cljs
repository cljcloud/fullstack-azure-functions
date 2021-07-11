(ns fullstack-azure-functions.server.db
  (:require [fullstack-azure-functions.cljcloud.cljs-mssql :as sql]
            [environ.core :refer [env]]))

(def conn-str (env :mssql-conn-str))

(def conn-pool (delay (sql/connect conn-str)))

(defn get-products []
  (sql/query @conn-pool "select * from products"))

