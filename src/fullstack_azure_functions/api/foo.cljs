(ns fullstack-azure-functions.api.foo
  (:require [cljs.nodejs :as nodejs]
            ["fs" :as fs]
            [environ.core :refer [env]]
            [cognitect.transit :as t]
    ;["md5" :as md5]
            )
  )


;; JavaScript Interop
;; http://www.spacjer.com/blog/2014/09/12/clojurescript-javascript-interop/

;; What are the .. ?
;; (.method object) ; Equivalent to object.method()
;; (.-property object) ; Equivalent to object[property]

;; (.. object -property -property method) ; object[property][property][method]()
;; (.. object -property -property -property) ; object[property][property][property]

;; Helpers

(defn utc-now []
  (js/Date.))

;; Respond with Transit JSON - consume as CLJ data struct
(def transit-writer (t/writer :json))


(defn http-ok [res]
  {:status  200
   :body    (t/write transit-writer res)
   :headers {
             "Content-Type"                "application/transit+json"
             ;"Cache-Control" "public,max-age=31536000"
             "Access-Control-Allow-Origin" "*"
             }
   }
  )

;; TODO: Auto generate swagger json and UI
;; TODO: Server-side rendering Azure Function
;; TODO: Clojars Library - Easily create fullstack azure functions with ClojureScript



(defn run
  {:doc            "FIXME: write documentation"
   :azure/disabled false
   :azure/bindings [{:authLevel "function"
                     :type      "httpTrigger"
                     :direction "in"
                     :name      "req"
                     :methods   ["get" "post"]}
                    {:type      "http"
                     :direction "out"
                     :name      "res"}]}
  [^js context ^js req]
  (.log context "Cljs Azure Function Run")
  (let [name (or (.. req -query -name)
                 (and (.. req -body) (.. req -body -name)))
        result
             (http-ok {:id      1
                       :bar     true
                       :now     (utc-now)
                       :welcome (or name "anonymous")
                       :details {
                                 :id 123
                                 :type :user
                                 :roles #{:admin :user}
                                 }
                       })]
    ;;set response context.res = {...}
    (set! (. context -res) (clj->js result))
    ;; signal that async func finished
    (. context done))

  )

(comment
  (shadow.cljs.devtools.api/repl :azure)
  (nodejs/enable-util-print!)
  (fs/readFileSync "host.json" "utf8")
  (env :functions-worker-runtime)
  (env :azure-web-jobs-storage)
  (env :database-url)
  )