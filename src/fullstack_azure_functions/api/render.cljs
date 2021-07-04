(ns fullstack-azure-functions.api.render
  (:require [cljs.nodejs :as nodejs]
            ["fs" :as fs]
            [environ.core :refer [env]]
            [cognitect.transit :as t]
            [reagent.dom.server :as r]
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



(defn template [{:keys [local?]}]
  [:html {:lang "en" :data-color-mode "light" :data-dark-theme "light"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:link {:href "https://unpkg.com/@primer/css/dist/primer.css" :rel "stylesheet"}]
    [:style {:type "text/css"} "body [role=button]:focus,
        body [role=tabpanel][tabindex=\"0\"]:focus,
        body a:focus,
        body button:focus,
        body summary:focus {
            outline: none;
            box-shadow: none;
        }

        .pagehead-actions>li {
            float: left;
            margin: 0 10px 0 0;
            font-size: 11px;
            color: var(--color-text-primary);
            list-style-type: none;
        }"]
    [:title "CljCloud - Fullstack Azure Functions"]]
   [:body
    [:div#app]
    [:script {:src (if local?
                     "http://localhost:8020/js/app.js"
                     "/js/app.js"
                     )}]
    [:script "fullstack_azure_functions.core.init();"]]]
  )

(defn render-page []
  {:status  200
   :body    (r/render-to-static-markup [template {:local? true}])
   :headers {
             "Content-Type"                "text/html"
             ;"Cache-Control" "public,max-age=31536000"
             "Access-Control-Allow-Origin" "*"
             }
   }
  )

;; TODO: Auto generate swagger json and UI
;; TODO: Server-side rendering Azure Function
;; TODO: Clojars Library - Easily create fullstack azure functions with ClojureScript
;; [cljcloud/azure-functions-cljs "0.0.1"]



(defn run
  {:doc            "FIXME: write documentation"
   :azure/disabled false
   :azure/bindings [{:authLevel "function"
                     :type      "httpTrigger"
                     :direction "in"
                     :name      "req"
                     :methods   ["get"]
                     :route     "{*path}"}
                    {:type      "http"
                     :direction "out"
                     :name      "res"}]}
  [^js context ^js req]
  (.log context "Cljs Azure Function Run")
  (let [name   (or (.. req -query -name)
                   (and (.. req -body) (.. req -body -name)))
        result (render-page)

        ]
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