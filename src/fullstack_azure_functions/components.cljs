(ns fullstack-azure-functions.components
  (:require ;[ajax.core :refer [GET POST]]
            [cross-fetch :as fetch]
            [cognitect.transit :as t]
            [reitit.core :as r]
            [reitit.frontend :as rf]
            [fullstack-azure-functions.state :as s]))

(def transit-json-reader
  (delay (t/reader :json)))

(defn name->href
  "Isomorphic version of rfe/href."
  [name]
  (let [match (rf/match-by-name! @s/router name)]
    (r/match->path match)))

;; TODO: use isomorphic-fetch
;; TODO: transcode transit to clj code https://github.com/lambdaisland/fetch/blob/main/src/lambdaisland/fetch.cljs
;; TODO: add ^js in front of response to make sure that .-status and .json are not munged during :advanced optimizations.

(defn get-api-data []
  (-> (fetch "http://localhost:8021/api/users")
      (.then #(.text %))
      (.then (fn [res]
               (prn [:get-api-data-res res])
               (let [details (:details res)]
                 (prn "details" details)
                 (swap! s/app-state assoc :api-data
                        (-> (t/read @transit-json-reader res)
                            (js->clj :keywordize-keys true)
                            )
                        )
                 ))))
    ;(GET "http://localhost:8021/api/users"
    ;   {;:headers       (->headers)
    ;    ;:params        params
    ;    :error-handler (fn [err] (prn [:get-api-data-error err]))
    ;    :handler       (fn [res]
    ;                     (prn [:get-api-data-res res])
    ;                     (let [details (:details res)]
    ;                       (prn "details" details)
    ;                       (swap! s/app-state assoc :api-data res)
    ;                       ))})
  )

(defn get-products []
  (-> (fetch "http://localhost:8021/api/products")
      (.then #(.text %))
      (.then (fn [res]
               (prn [:get-api-data-res res])
               (let [details (:details res)]
                 (prn "details" details)
                 (swap! s/app-state assoc :products
                        (-> (t/read @transit-json-reader res)
                            (js->clj :keywordize-keys true)
                            )
                        )
                 ))))


  ;(GET "http://localhost:8021/api/products"
  ;     {:error-handler (fn [err] (prn [:get-products-error err]))
  ;      :handler       (fn [res]
  ;                       (prn [:get-products-res res])
  ;                       (swap! s/app-state assoc :products res))})
  )

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
   [:h1.text-center.pt-6.f00-light "Products page"]
   (let [products (:products @s/app-state)]
     (if (seq products)
       [:pre (with-out-str (cljs.pprint/pprint products))]
       (do
         (get-products)
         [:h2 "Loading..."])))])

(defn contact-page []
  [:div.container-md.clearfix.anim-scale-in
   [:h1.text-center.pt-6.f00-light "Contact page"]])

