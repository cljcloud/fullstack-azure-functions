(ns fullstack-azure-functions.cljcloud.azure
  #?(:cljs (:require [goog :as goog]
                     [goog.object :as gobject]))
  #?(:clj (:require [clojure.java.io :as io]
                    [jsonista.core :as j]
                    [environ.core :refer [env]]
                    [clojure.string :as s])))

;; Cljs macros
;; https://code.thheller.com/blog/shadow-cljs/2019/10/12/clojurescript-macros.html
;; Here we use cljc version, see Gotcha #5: CLJC.

#?(:cljs
   (defn obj->clj
     "Recursively convert any JS object into Clojure map.

     js->clj only converts simple js objects,
     some objects created using c'tor function (e.g. InvocationContext)
     are not being converted to CLJ, this helper does it."
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

(defmacro defhttp
  "Define new Azure Function with http trigger."
  [name & {:keys [methods route handler auth]
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
   (defn build-hook
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
