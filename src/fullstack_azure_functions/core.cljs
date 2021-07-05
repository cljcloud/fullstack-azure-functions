(ns fullstack-azure-functions.core
  (:require [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [react-dom :as react-dom]
            [fullstack-azure-functions.pages :as p])
  )

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  ;(routes/init!)
  (js/console.log "on start")
  (rdom/render [p/app] (.getElementById js/document "app")))


(defn ^:export hydrate [state]
  (js/console.log "on hydrate" state)
  ;; hydrate is called ONCE when the page loads
  ;; this is called in the SSR html output and must be exported
  ;; so it is available even in :advanced release builds
  ;; (js/console.log
  ;;   (str "%c" cybertron-ascii-art)
  ;;   "background: #42cf00; color: #c2ff00;")

  ;; hydrate state
  (reset! p/app-state (js->clj state :keywordize-keys true))

  ;; hydrate on init to avoid re-render
  (react-dom/hydrate (r/as-element [p/app]) (.getElementById js/document "app")))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "on stop"))


(comment
  (shadow.cljs.devtools.api/repl :app)
  (enable-console-print!)
  )

