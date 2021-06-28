(defproject fullstack-azure-functions "0.1.0-SNAPSHOT"
  :description "Fullstack serverless app built on Azure Functions and Storage"
  :url "https://github.com/cljcloud/fullstack-azure-functions"
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/core.async "1.3.610"]
                 [metosin/jsonista "0.3.3"]
                 [environ "1.2.0"]
                 [cljs-ajax "0.8.1"]
                 [com.google.javascript/closure-compiler-unshaded "v20200504" :scope "provided"]
                 [thheller/shadow-cljs "2.11.7" :scope "provided"]
                 [reagent "1.0.0"]]
  :plugins [[lein-shadow "0.4.0"]
            [lein-environ "1.2.0"]]
  :main ^:skip-aot fullstack-azure-functions.core
  :target-path "target/%s"
  :shadow-cljs {:nrepl    {:port 7002}
                ;; dev server, serves static files
                :dev-http {8020 ["resources/public" "target/app"]}
                :builds   {:app   {:target     :browser
                                   :output-dir "target/app/js"
                                   :asset-path "/js"
                                   :modules    {:app {:entries [fullstack-azure-functions.core]}}}
                           :azure {:target      :azure-app
                                   :fn-map      {:foo fullstack-azure-functions.api.foo/run}
                                   :app-dir     "target/azure"
                                   :build-hooks [(fullstack-azure-functions.shadow-cljs.azure-app/inject-settings
                                                   {:host     {:test    "test 123"
                                                               :number  42
                                                               :enabled true}
                                                    :settings {:env [:database-url]}})]
                                   :js-options  {:js-provider          :shadow
                                                 :keep-native-requires true}}
                           :test  {:target    :node-test
                                   :output-to "target/test/test.js"
                                   :autorun   true}}}
  :npm-deps []
  :npm-dev-deps [[xmlhttprequest "1.8.0"]
                 [ws "7.5.0"]
                 [source-map-support "0.5.19"]]

  :profiles {:prod {:env {:database-url "production-db"}}
             :dev  {:env {:database-url "jdbc:postgresql://localhost/dev"}}})
