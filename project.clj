(defproject fullstack-azure-functions "0.1.0-SNAPSHOT"
  :description "Fullstack serverless app built on Azure Functions and Storage"
  :url "https://github.com/cljcloud/fullstack-azure-functions"
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/core.async "1.3.610"]
                 ;; json
                 [metosin/jsonista "0.3.3"]
                 ;; env
                 [environ "1.2.0"]
                 ;; cljs
                 [cljs-ajax "0.8.1"]
                 [com.cognitect/transit-cljs "0.8.269"]
                 [reagent "1.0.0"]
                 ;; routing
                 [metosin/reitit "0.5.13"]
                 ;; db migrations
                 [migratus "1.3.5"]
                 [com.microsoft.sqlserver/mssql-jdbc "9.2.1.jre8"]


                 ;; installed locally
                 [com.google.javascript/closure-compiler-unshaded "v20200504" :scope "provided"]
                 [thheller/shadow-cljs "2.11.7" :scope "provided"]
                 ]
  :plugins [[lein-shadow "0.4.0"]
            [lein-environ "1.2.0"]
            [migratus-lein "0.7.3"]
            [lein-shell "0.5.0"]]

  ;:main ^:skip-aot fullstack-azure-functions.core
  :target-path "target/%s"
  :shadow-cljs {:nrepl    {:port 7002}
                ;; dev server, serves static files
                :dev-http {8020 ["resources/public" "target/app"]}
                :builds   {:app   {:target     :browser
                                   :output-dir "target/app/assets/js"
                                   :asset-path "/assets/js"
                                   :modules    {:app {:entries [fullstack-azure-functions.core]}}}
                           :azure {:target      :azure-app
                                   ;; the order of fn definition is always abc
                                   ;; important to have wildcard routes defined last
                                   :fn-map      {:users    fullstack-azure-functions.server.azure-fns/users
                                                 :products fullstack-azure-functions.server.azure-fns/products
                                                 :z-ssr    fullstack-azure-functions.server.azure-fns/ssr}
                                   :app-dir     "target/azure"
                                   :build-hooks [(fullstack-azure-functions.cljcloud.azure/build-hook)]
                                   :js-options  {:js-provider          :shadow
                                                 :keep-native-requires true
                                                 :keep-as-require      #{"mssql"}}}
                           :test  {:target    :node-test
                                   :output-to "target/test/test.js"
                                   :autorun   true}}}

  :npm-deps [[mssql "7.1.3"]
             [cross-fetch "3.1.4"]]

  :npm-dev-deps [[xmlhttprequest "1.8.0"]
                 [ws "7.5.0"]
                 [source-map-support "0.5.19"]]

  :migratus {:store         :database
             :migration-dir "migrations"
             :db            ~(get (System/getenv) "jdbc-conn-str")}

  :shell {:dir "target/azure"}

  :aliases {"watch"        ["shadow" "watch" "azure" "app"]
            "azure"        ["shell" "func" "start" "--cors" "*" "--port" "8021"]
            "release:prod" ["with-profile" "prod" "shadow" "release" "azure" "app"]}

  ;; real values inside local profiles.clj
  :profiles {:prod {:env {:jdbc-conn-str  "production-db"
                          :mssql-conn-str "mssql-conn-str"
                          :proxy-assets   "storage-url"
                          :proxy-favicon  "storage-url"}}
             :dev  {:env {:jdbc-conn-str  "local-db-url"
                          :mssql-conn-str "mssql-conn-str"
                          :proxy-assets   "http://localhost:8020/assets/{path}"
                          :proxy-favicon  "http://localhost:8020/favicon.ico"}}})
