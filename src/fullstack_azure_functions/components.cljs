(ns fullstack-azure-functions.components
  (:require [ajax.core :refer [GET POST]]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(def app-state (r/atom {}))

(defn get-api-data []
  (GET "http://localhost:8021/api/users"
       {;:headers       (->headers)
        ;:params        params
        :error-handler (fn [err] (prn [:get-api-data-error err]))
        :handler       (fn [res]
                         (prn [:get-api-data-res res])
                         (let [details (:details res)]
                           (prn "details" details)
                           (reset! app-state res)
                           ))}))

(defn header-nav []
  [:div.Header.px-6.color-bg-secondary
   [:div.Header-item.mr-6
    [:a.Header-link.f4.d-flex.flex-items-center.color-text-primary {:href "#"}
     ;[icons/render :cpu 32 32]
     [:span "FullStack Azure Functions App"]]]
   [:div.Header-item.ml-6.mr-2
    [:a.Header-link.color-text-primary {:href "#"} "APIs"]]])

(defn app []
  (prn "render app")
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

