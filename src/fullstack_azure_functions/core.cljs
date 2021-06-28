(ns fullstack-azure-functions.core
  ;(:require [cybertron.routes :as routes])
  )

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  ;(routes/init!)
  (js/console.log "start2" (+ 2 2))
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
  )

