(ns fullstack-azure-functions.api.users
  (:require [cljs.nodejs :as nodejs]
            ["fs" :as fs]
            [environ.core :refer [env]]
            [cognitect.transit :as t]
            ))

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

(defn run
  {:doc            "FIXME: write documentation"
   :azure/disabled false
   :azure/bindings [{:authLevel "function"
                     :type      "httpTrigger"
                     :direction "in"
                     :name      "req"
                     :methods   ["get"]
                     :route     "api/users"}
                    {:type      "http"
                     :direction "out"
                     :name      "res"}]}
  [^js context ^js req]
  (.log context "Cljs Azure Function Run")
  (let [name   (or (.. req -query -name)
                   (and (.. req -body) (.. req -body -name)))
        result
        (http-ok {:id      1
                  :bar     true
                  :now     (utc-now)
                  :welcome (or name "anonymous")
                  :details {
                            :id    123
                            :type  :user
                            :roles #{:admin :user}
                            }
                  })

        ]
    ;;set response context.res = {...}
    (set! (. context -res) (clj->js result))
    ;; signal that async func finished
    (. context done))
  )