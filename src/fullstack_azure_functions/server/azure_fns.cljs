(ns fullstack-azure-functions.server.azure-fns
  (:require [environ.core :refer [env]]
            [cljs.nodejs :as nodejs]
            [cognitect.transit :as t]
            [fullstack-azure-functions.server.ssr :refer [render-app->html]]
            [fullstack-azure-functions.cljcloud.cljs-azure :refer-macros [defapi]]))


;; Respond with Transit JSON - consume as CLJ data struct
(def transit-writer (t/writer :json))

;; Helpers

(defn utc-now []
  (js/Date.))

(defn clj->json [data]
  (t/write transit-writer data))

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

(defapi ssr
        :methods ["get"]
        :route "{*path}"
        :handler (fn [ctx req res]
                   (prn [:ssr-invoked req])
                   (->> req
                        render-app->html
                        html-ok
                        res)))

;; cljcloud api fn azure handler
(defapi roles
        :route "api/roles"
        :handler (fn [ctx req res]
                   (prn [:roles-invoked req])
                   ;; ctx and req are usual clojure maps
                   ;(cljs.pprint/pprint [:roles-invoked :ctx ctx :req req])
                   ;(prn "test")
                   ;(prn (-> ctx
                   ;         :bindings
                   ;         :req
                   ;         :headers))
                   (->> [{:id 1 :name "user"}
                         {:id 2 :name "admin"}]
                        clj->json
                        json-ok
                        res)))


;; shadow-cljs default azure fn handler
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
  (let [name   (or (.. req -query -name)
                   (and (.. req -body) (.. req -body -name)))
        result {:id      1
                :bar     true
                :now     (utc-now)
                :welcome (or name "anonymous")
                :details {:id    123
                          :type  :user
                          :roles #{:admin :user}}}]
    (->> result
         (t/write transit-writer)
         json-ok
         clj->js
         (set! (.-res ctx)))
    (.done ctx)))

(comment
  ;; run server REPL, run func start to start nodejs process runtime
  (shadow.cljs.devtools.api/repl :azure)
  (nodejs/enable-util-print!)
  (fs/readFileSync "host.json" "utf8")
  (env :functions-worker-runtime)
  (env :azure-web-jobs-storage)
  (env :database-url)

  (macroexpand '(defapi ssr
                        :methods ["get"]
                        :route "{*path}"
                        :handler (fn [ctx req res]
                                   (prn [:ssr-invoked req])
                                   (->> (render-app->html)
                                        html-ok
                                        res))))
  )