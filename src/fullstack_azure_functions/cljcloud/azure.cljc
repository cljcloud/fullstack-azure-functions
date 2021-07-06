(ns fullstack-azure-functions.cljcloud.azure
  #?(:cljs (:require [goog :as goog]
                     [goog.object :as gobject])))

;; ClojureScript rules say:
;; -- you can only use it in what we call a macro namespace, effectively forcing you to separate your compile time and runtime code

;; http://cljs.github.io/api/cljs.core/defmacro

;; ---=== ClojureScript Quirks ===---
;;
;; Despite in (prn obj) the key printed as keyword
;; Access (:method obj) doesn't work, must use (.-method obj)
;;  #js {:method "GET"}

;; JavaScript Interop
;; http://www.spacjer.com/blog/2014/09/12/clojurescript-javascript-interop/

;; What are the .. ?
;; (.method object) ; Equivalent to object.method()
;; (.-property object) ; Equivalent to object[property]

;; (.. object -property -property method) ; object[property][property][method]()
;; (.. object -property -property -property) ; object[property][property][property]

;; ---=== Azure Functions ===---
;;
;; https://docs.microsoft.com/en-us/azure/azure-functions/functions-reference-node

;; simple defn with meta
;(defmacro def-api [name methods route & defn-args]
;  `(defn ~(vary-meta name assoc
;                     :azure/bindings [{:authLevel "anonymous"
;                                       :type      "httpTrigger"
;                                       :direction "in"
;                                       :name      "req"
;                                       :methods   methods
;                                       :route     route}
;                                      {:type      "http"
;                                       :direction "out"
;                                       :name      "res"}])
;     ~@defn-args))

;; ctx has a custom type - Function c'tor - InvocationContext
;; js->clj only converts simple js objects

#?(:cljs
   (defn obj->clj
     "Recursively convert any JS object into Clojure map."
     [obj]
     (if (goog/isObject obj)
       (persistent!
         (reduce (fn [r k]
                   (let [v (gobject/get obj k)]
                     (if (= "function" (goog/typeOf v))
                       r
                       (assoc! r (keyword k) (obj->clj v)))))
                 (transient {})
                 (gobject/getKeys obj)))
       obj)))


(defmacro defapi [name & {:keys [methods route handler auth]
                          :or   {methods ["get"]
                                 auth    "anonymous"}}]
  `(defn ~(vary-meta name assoc
                     :azure/bindings [{:authLevel auth
                                       :type      "httpTrigger"
                                       :direction "in"
                                       :name      "req"
                                       :methods   methods
                                       :route     route}
                                      {:type      "http"
                                       :direction "out"
                                       :name      "res"}])
     [^js/Object ctx# req#]
     ;; override console.log with context.log - log to Azure
     (set! (.-log js/console) (.-log ctx#))
     ;; create a res fn to async respond with result
     (let [res# (fn [r#]
                  (->> r#
                       cljs.core/clj->js
                       (set! (.-res ctx#)))
                  (.done ctx#))]
       (~handler (obj->clj ctx#) (obj->clj req#) res#))))
