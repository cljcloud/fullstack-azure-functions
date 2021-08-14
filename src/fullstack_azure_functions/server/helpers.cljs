(ns fullstack-azure-functions.server.helpers
  (:require [cognitect.transit :as t]))

;; Respond with Transit JSON - consume as CLJ data struct
(def transit-writer (t/writer :json))

(defn clj->transit [data]
  (t/write transit-writer data))
