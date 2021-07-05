(ns fullstack-azure-functions.core
  (:require [ajax.core :refer [GET POST]]
            [reagent.dom :as rdom]
            [fullstack-azure-functions.pages :as p])
  )

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  ;(routes/init!)
  (rdom/render [p/app] (.getElementById js/document "app"))
  (js/console.log "started")
  )

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  ;; (js/console.log
  ;;   (str "%c" cybertron-ascii-art)
  ;;   "background: #42cf00; color: #c2ff00;")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))


(comment
  (shadow.cljs.devtools.api/repl :app)
  (enable-console-print!)
  )

