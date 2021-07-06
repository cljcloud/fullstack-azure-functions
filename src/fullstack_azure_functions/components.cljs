(ns fullstack-azure-functions.components
  (:require [ajax.core :refer [GET POST]]
            [reitit.core :as r]
            [reitit.frontend :as rf]
            [fullstack-azure-functions.state :as s]))

(defn name->href
  "Isomorphic version of rfe/href."
  [name]
  (let [match (rf/match-by-name! @s/router name)]
    (r/match->path match)))

(defn get-api-data []
  (GET "http://localhost:8021/api/users"
       {;:headers       (->headers)
        ;:params        params
        :error-handler (fn [err] (prn [:get-api-data-error err]))
        :handler       (fn [res]
                         (prn [:get-api-data-res res])
                         (let [details (:details res)]
                           (prn "details" details)
                           (swap! s/app-state assoc :api-data res)
                           ))}))

(defn header-nav []
  [:div.Header.px-6.color-bg-secondary
   [:div.Header-item.mr-6
    [:a.Header-link.f4.d-flex.flex-items-center.color-text-primary {:href (name->href :routes/home)}
     [:span "FullStack Azure Functions App"]]]
   [:div.Header-item.ml-6.mr-2
    [:a.Header-link.color-text-primary {:href (name->href :routes/products)} "Products"]]
   [:div.Header-item.ml-6.mr-2
    [:a.Header-link.color-text-primary {:href (name->href :routes/contact)} "Contact"]]])

(defn- render-view [route]
  ;(prn [:render-view route])
  (let [public? (-> route :data :public?)
        view    (-> route :data :view)]
    (if public?
      [view route]
      ;; redirect to login page
      [:h1 "route not public"])))

(defn app []
  (prn [:render-app])
  [:<>
   [header-nav]
   (let [r (:route @s/app-state)]
     (render-view r))])

(defn home-page []
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
    (with-out-str (cljs.pprint/pprint (dissoc @s/app-state :route)))]])

(defn products-page []
  [:div.container-md.clearfix.anim-scale-in
   [:h1.text-center.pt-6.f00-light "Products page"]])

(defn contact-page []
  [:div.container-md.clearfix.anim-scale-in
   [:h1.text-center.pt-6.f00-light "Contact page"]])

