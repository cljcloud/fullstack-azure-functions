(ns fullstack-azure-functions.server.azure-fns
  (:require [environ.core :refer [env]]
            [cljs.nodejs :as nodejs]
            [cognitect.transit :as t]
            [fullstack-azure-functions.server.ssr :refer [render-app->html]]))

;; JavaScript Interop
;; http://www.spacjer.com/blog/2014/09/12/clojurescript-javascript-interop/

;; What are the .. ?
;; (.method object) ; Equivalent to object.method()
;; (.-property object) ; Equivalent to object[property]

;; (.. object -property -property method) ; object[property][property][method]()
;; (.. object -property -property -property) ; object[property][property][property]


;; Respond with Transit JSON - consume as CLJ data struct
(def transit-writer (t/writer :json))

;; Helpers

(defn utc-now []
  (js/Date.))

(defn json-ok [json]
  {:status  200
   :body    json
   :headers {"Content-Type"                "application/transit+json"
             "Access-Control-Allow-Origin" "*"}})

(defn html-ok [html]
  {:status  200
   :body    html
   :headers {"Content-Type"                "text/html; charset=utf-8"
             "Access-Control-Allow-Origin" "*"}})

(defn override-console-log!
  "Sets console.log to context.log.
  To enable debugging in Azure Function.
  Needs to be invoked inside function body.
  After all print-ln output will be redirected to Azure Host Logger."
  [ctx]
  (set! (.-log js/console) (.-log ctx))
  (nodejs/enable-util-print!))

;; Functions

(defn ssr
  {:azure/bindings [{:authLevel "function"
                     :type      "httpTrigger"
                     :direction "in"
                     :name      "req"
                     :methods   ["get"]
                     :route     "{*path}"}
                    {:type      "http"
                     :direction "out"
                     :name      "res"}]}
  [^js ctx req]
  (override-console-log! ctx)
  (prn [:ssr-invoked req])
  (let [result (render-app->html)]
    ;;set response context.res = {...}
    (set! (. ctx -res) (->> result
                                html-ok
                                clj->js))
    ;; signal that async func finished
    (. ctx done)))

(defn users
  {:azure/bindings [{:authLevel "function"
                     :type      "httpTrigger"
                     :direction "in"
                     :name      "req"
                     :methods   ["get"]
                     :route     "api/users"}
                    {:type      "http"
                     :direction "out"
                     :name      "res"}]}
  [^js ctx req]
  (override-console-log! ctx)
  (prn [:users-invoked req])
  ;(.log context "Cljs Azure Function Run")
  (let [name   (or (.. req -query -name)
                   (and (.. req -body) (.. req -body -name)))
        result {:id      1
                :bar     true
                :now     (utc-now)
                :welcome (or name "anonymous")
                :details {:id    123
                          :type  :user
                          :roles #{:admin :user}}}]
    ;;set response context.res = {...}
    (set! (. ctx -res)
          (->> result
               (t/write transit-writer)
               json-ok
               clj->js))
    ;; signal that async func finished
    (. ctx done)))

(comment
  ;; run server REPL, run func start to start nodejs process runtime
  (shadow.cljs.devtools.api/repl :azure)
  (nodejs/enable-util-print!)
  (fs/readFileSync "host.json" "utf8")
  (env :functions-worker-runtime)
  (env :azure-web-jobs-storage)
  (env :database-url)
  )