(ns fullstack-azure-functions.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [react-dom :as react-dom]
            [cognitect.transit :as t]
            [fullstack-azure-functions.components :as c]
            [fullstack-azure-functions.state :as s]
            [fullstack-azure-functions.routes :as routes]))

(defn ^:dev/after-load start
  "Start is called by init and after code reloading finishes"
  []
  (prn [:start])
  (routes/init)
  (rdom/render
    [c/app]
    (.getElementById js/document "app")))

(defn ^:export hydrate
  "Hydrate is called only once on page re-load.
  Called from the ssr template script and must be exported,
  to preserve it's name even in :advanced release builds."
  [^js/String state]
  (prn [:hydrate state])
  ;; hydrate state
  (s/hydrate (t/read @c/transit-json-reader state))
  ;; will update state with current route, required for further hydration
  (routes/init)
  ;; state must be same as used on server upon render, otherwise throws a warning
  ;; will trigger app render, supposedly to attach react handlers
  (react-dom/hydrate
    (r/as-element [c/app])
    (.getElementById js/document "app")))

(defn ^:dev/before-load stop
  "Stop called before any code is reloaded"
  []
  (prn [:stop]))


(comment
  ;; run client REPL, open browser to start runtime
  (shadow.cljs.devtools.api/repl :app)
  (enable-console-print!)
  )

