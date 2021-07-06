(ns fullstack-azure-functions.routes
  (:require [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [fullstack-azure-functions.components :as c]
            [fullstack-azure-functions.state :as s]))

(def routes
  [["/"
    {:name    :routes/home
     :public? true
     :view    c/home-page}]

   ["/products"
    {:name    :routes/products
     :public? true
     :view    c/products-page}]

   ["/contact"
    {:name    :routes/contact
     :public? true
     :view    c/contact-page}]])

(defn init-router []
  (reset! s/router (rf/router routes {:data {:coercion rss/coercion}})))

(defn init []
  (init-router)
  (rfe/start!
    @s/router
    (fn [route]
      ;(prn [:change-route route])
      (swap! s/app-state assoc :route route))
    {:use-fragment false}))
