

(ns fullstack-azure-functions.api.foo
  (:require [cljs.nodejs :as nodejs]
            ["fs" :as fs]
            [environ.core :refer [env]]
            ;["md5" :as md5]
            )
  )


(defn run
  {:doc "FIXME: write documentation"
   :azure/disabled false
   :azure/bindings
   [{:authLevel "function"
     :type "httpTrigger"
     :direction "in"
     :name "req"
     :methods ["get" "post"]}
    {:type "http"
     :direction "out"
     :name "res"}]}
  [^js context ^js req]
  (.log context "Cljs Azure Function Run")
  (let [name (or (.. req -query -name)
                 (and (.. req -body) (.. req -body -name)))
        result
        (if (seq name)
          {:status 200
           :body (str "CLJS: Hello " name)}
          {:status 200
           :body "bar"})]
    ;;set response context.res = {...}
    (set! (. context -res) (clj->js result))
    ;; signal that async func finished
    (. context done)))

(comment
  (shadow.cljs.devtools.api/repl :azure)
  (nodejs/enable-util-print!)
  (fs/readFileSync "host.json" "utf8")
  (env :functions-worker-runtime)
  (env :azure-web-jobs-storage)
  (env :database-url)
  )