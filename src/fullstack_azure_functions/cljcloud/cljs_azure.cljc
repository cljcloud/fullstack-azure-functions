(ns fullstack-azure-functions.cljcloud.cljs-azure
  #?(:cljs (:require [goog :as goog]
                     [goog.object :as gobject]))
  #?(:clj (:require [clojure.java.io :as io]
                    [jsonista.core :as j]
                    [environ.core :refer [env]]
                    [clojure.string :as s])))

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

#?(:clj
   (defn- render-template [template]
     (->> template
          (reduce-kv
            (fn [m k v]
              (if (map? v)
                (assoc m k (render-template v))
                (if (and (string? v)
                         (s/starts-with? v "{")
                         (s/ends-with? v "}"))
                  (let [val (subs v 1 (dec (.length v)))]
                    (assoc m k (env (keyword val))))
                  (assoc m k v))))
            {}))))

#?(:clj
   (defn render-settings
     "Renders Azure Function settings files:
       host.json, local.settings.json, proxies.json

     Replaces template values with environment values.
       e.g. MY_KEY: '{my-key-value}' -> MY_KEY: 'value from (:my-key-value env)'

     Saves files to :app-dir configured in shadow-cljs.edn"
     {:shadow.build/stage :configure}
     [build-state & args]
     (let [target  (:shadow.build/config build-state)
           app-dir (:app-dir target)]
       (let [host     (-> "resources/host.tmpl.json"
                          slurp
                          (j/read-value j/keyword-keys-object-mapper))
             settings (-> "resources/local.settings.tmpl.json"
                          slurp
                          (j/read-value j/keyword-keys-object-mapper))
             proxies  (-> "resources/proxies.tmpl.json"
                          slurp
                          (j/read-value j/keyword-keys-object-mapper))]
         ;; create app-dir
         (.mkdir (io/file app-dir))
         ;; write settings files
         (->> host
              render-template
              j/write-value-as-string
              (spit (str app-dir "/host.json")))

         (->> settings
              render-template
              j/write-value-as-string
              (spit (str app-dir "/local.settings.json")))

         (->> proxies
              render-template
              j/write-value-as-string
              (spit (str app-dir "/proxies.json")))))
     build-state))
