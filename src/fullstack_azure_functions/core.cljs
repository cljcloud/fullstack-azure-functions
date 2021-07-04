(ns fullstack-azure-functions.core
  (:require [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reagent.dom :as rdom])
  )

(def app-state (r/atom {}))

;@app-state

(defn get-api-data []
  (GET "http://localhost:8021/api/foo"
       {;:headers       (->headers)
        ;:params        params
        :error-handler (fn [err] (prn [:get-api-data-error err]))
        :handler       (fn [res]
                         (prn [:get-api-data-res res])
                         (let [details (:details res)]
                           (prn "details" details)
                           (reset! app-state res)
                           )
                         )})
  )

(defn header-nav []
  [:div.Header.px-6.color-bg-secondary
   [:div.Header-item.mr-6
    [:a.Header-link.f4.d-flex.flex-items-center.color-text-primary {:href "#"}
     ;[icons/render :cpu 32 32]
     [:span "FullStack Azure Functions App"]]]
   [:div.Header-item.ml-6.mr-2
    [:a.Header-link.color-text-primary {:href "#"} "APIs"]]])

(defn app []
  [:<>
   [header-nav]
   [:div.container-md.clearfix.anim-scale-in
    [:h1.text-center.pt-6.f00-light "Welcome"]
    [:div.flash.f4.mt-10
     "Please login"

     [:a.btn.primary.flash-action {:role "button"
                                   :href "#"}
      "Login"]
     ]
    [:br]
    [:button.btn.primary {:on-click get-api-data} "Get API Data"]
    [:br]
    [:br]
    [:b "App state:"]
    [:br]
    [:br]
    [:pre
     (with-out-str (cljs.pprint/pprint @app-state))
     ]
    ]])

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  ;(routes/init!)
  (rdom/render [app] (.getElementById js/document "app"))
  (js/console.log "started")
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
  (enable-console-print!)
  )

