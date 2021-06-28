(ns fullstack-azure-functions.shadow-cljs.azure-app
  (:require [clojure.java.io :as io]
            [jsonista.core :as j]
            [environ.core :refer [env]]))


;; Pipe input stream to output stream, uses buffer
;; do not save entire bytes to memory
(defn copy-uri [uri file]
  (with-open [in  (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))

(defn copy-file [src dest]
  (io/copy (io/file src) (io/file dest)))

(defn inject-settings
  {:shadow.build/stage :configure}
  [build-state & args]
  (let [target   (:shadow.build/config build-state)
        app-dir  (:app-dir target)
        config   (first args)
        host     (:host config)
        settings (:settings config)]
    (let [default-host      (-> "resources/host.json"
                                slurp
                                (j/read-value j/keyword-keys-object-mapper))
          merged-host       (merge default-host host)
          default-settings  (-> "resources/local.settings.json"
                                slurp
                                (j/read-value j/keyword-keys-object-mapper))
          env-settings-keys (:env settings)
          merged-settings   (->> env-settings-keys
                                 (reduce (fn [settings key]
                                           (assoc-in settings [:Values key] (env key)))
                                         default-settings))]
      (.mkdir (io/file app-dir))
      (spit (str app-dir "/host.json") (j/write-value-as-string merged-host))
      (spit (str app-dir "/local.settings.json") (j/write-value-as-string merged-settings))))
  build-state)
