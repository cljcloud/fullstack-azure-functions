(ns fullstack-azure-functions.cljcloud.cljs-mssql
  (:require [mssql :as sql]
            [environ.core :refer [env]]
            [cljs.core.async :refer [<! chan >!] :refer-macros [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [goog.object :as gobject]))

;; All is async here, returns promise, must be catched, otherwise nodejs process crash

;; https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f
;;


(defn suppress [f]
  (.catch f #(prn [:error %])))

;; Custom connection pool
;; https://tediousjs.github.io/node-mssql

(defn connect [conn-str]
  (.connect sql conn-str))

(defn query
  ([conn sql-query] (query conn sql-query nil))
  ([conn sql-query c]
   (-> conn
       (.then #(.request %))
       (.then #(.query % sql-query))
       (.then #(let [res (-> %
                             (gobject/get "recordset")
                             (js->clj :keywordize-keys true))]
                 ;; TODO: kebab-case keys
                 (if c
                   (go (>! c res))
                   res))))))

(comment

  ;; run server REPL, run func start to start nodejs process runtime
  (shadow.cljs.devtools.api/repl :azure)

  (def conn-pool (delay (sql/connect (env :mssql-conn-str))))

  ;; example usage
  ;; results are printed to nodejs process console, not the REPL
  ;; since it executed asynchronously

  ;; with channel
  (go
    (let [results-chan (chan)]
      (sql/query @conn-pool "select * from products" results-chan)
      (prn (<! results-chan))))

  ;; without channel
  (go
    (prn (<p! (sql/query @conn-pool "select * from products"))))

  ;; if promise throws an error without catch
  ;; UnhandledPromiseRejection crashes NodeJS process
  (go
    (let [results-chan (chan)]
      (sql/query @conn-pool "select * from products2" results-chan)
      (prn (<! results-chan))))

  ;; we can suppress error and log to console
  (go
    (let [results-chan (chan)]
      (suppress (sql/query @conn-pool "select * from products2" results-chan))
      (prn (<! results-chan))))

  ;; or we can handle the error manually with .catch
  (go
    (let [results-chan (chan)]
      (-> (sql/query @conn-pool "select * from products2" results-chan)
          (.catch (fn [err]
                    (prn [:catched-error err])
                    ))
          )
      (prn (<! results-chan))))

  ;; or we can handle the error manually with <p! and try/catch
  (go
    (let [results-chan (chan)]
      (try
        (<p! (sql/query @conn-pool "select * from products2" results-chan))
        (catch :default err
          (prn [:catched-error err])))
      (prn (<! results-chan))))

  )

