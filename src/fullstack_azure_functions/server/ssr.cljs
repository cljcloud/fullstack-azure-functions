(ns fullstack-azure-functions.server.ssr
  (:require [reagent.dom.server :as r]
            [fullstack-azure-functions.components :as c]))

(defn template [app]
  [:html {:lang "en" :data-color-mode "light" :data-dark-theme "light"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:link {:href "https://unpkg.com/@primer/css/dist/primer.css" :rel "stylesheet"}]
    [:style {:type "text/css"} "body [role=button]:focus,
        body [role=tabpanel][tabindex=\"0\"]:focus,
        body a:focus,
        body button:focus,
        body summary:focus {
            outline: none;
            box-shadow: none;
        }

        .pagehead-actions>li {
            float: left;
            margin: 0 10px 0 0;
            font-size: 11px;
            color: var(--color-text-primary);
            list-style-type: none;
        }"]
    [:title "CljCloud - Fullstack Azure Functions"]]
   [:body
    [:div#app
     [app]]
    [:script {:src "/assets/js/app.js"}]
    [:script {:dangerouslySetInnerHTML
              {:__html (str "fullstack_azure_functions.core.hydrate("
                            (->> @c/app-state
                                 clj->js
                                 (.stringify js/JSON))
                            ");"
                            )}}]
    ]])


;; TODO: How to find what page to render?
;; Get URL, run reitit routing match, find page component
;; Server Side Rendering only for public pages, no password
;; If URL match to secure page? ->
;; render login screen with returnUrl - to be redirected if user logged in.
;; How to fetch required data
;; Each route should have a multi fn with :server and :client
;; to get the required data from db or from API

(defn render-app->html []
  (reset! c/app-state {:id  1,
                       :bar true})
  (r/render-to-string [template c/app]))


;; TODO: Auto generate swagger json and UI
;; TODO: Server-side rendering Azure Function
;; TODO: Clojars Library - Easily create fullstack azure functions with ClojureScript
;; [com.cljcloud/cljs-azure "0.0.1"]
;; (:require [cljs-azure.function :as az])





