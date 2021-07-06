(ns fullstack-azure-functions.state
  (:require [reagent.core :as r]))

(def app-state (r/atom {:route nil}))

(defn hydrate [^js/Object state]
  (reset! app-state
          (js->clj state :keywordize-keys true)))

(defn dehydrate
  "Removes non-serializable props, e.g. :route.
  Presumably to be hydrated on the client, e.g. via (routes/init)."
  []
  (dissoc @app-state :route))


;; ---=== Router ===---
;;
;; Extracted here to avoid circular dependency between routes and components

(def router (atom nil))
