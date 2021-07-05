(ns fullstack-azure-functions.cljcloud.shadow-cljs
  (:require [clojure.java.io :as io]
            [jsonista.core :as j]
            [environ.core :refer [env]]
            [clojure.string :as s]))

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
         {})))

(defn render-settings
  "Renders Azure Function settings files:
    host.json, local.settings.json, proxies.json

  Replaces template values with environment values.
    e.g. MY_KEY: '{MY_KEY_VALUE}' -> MY_KEY: 'MY_KEY_ENV_VALUE'

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
  build-state)

;(defmacro def-azure-fun
;
;  )


(comment

  (def-azure-fun
    {:trigger :http
     :methods :get
     :route   "{*path}"
     }
    [req ctx]

    )

  )
