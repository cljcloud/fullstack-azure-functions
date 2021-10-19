;; This is just an example of profiles.clj should look like
;; Create a copy via: `cp example.profiles.clj profiles.clj`
;; profiles.clj will not appear in changes, b/c it's added to .gitignore

{;; Default dev settings
 :dev  {
        :env {
              :jdbc-conn-str                     "jdbc:sqlserver://localhost:1433;database=my-sql-db;user=admin@server;password=secret;"
              :mssql-conn-str                    "Server=tcp:localhost,1433;Initial Catalog=my-sql-db;Persist Security Info=False;User ID=admin;Password=secret;MultipleActiveResultSets=False;Encrypt=True;"

              :proxy-assets                      "http://localhost:8020/assets/{path}"
              ;; this is needed to allow reference to localhost in proxies.json for Server Side Rendering
              :azure-function-disable-local-call "true"
              :azure-storage-conn-str            ""}
        }

 ;; production settings, used only if specified `with-profile prod`
 :prod {
        :env {
              :jdbc-conn-str                     "jdbc:sqlserver://localhost:1433;database=my-sql-db;user=admin@server;password=secret;"
              :mssql-conn-str                    "Server=tcp:localhost,1433;Initial Catalog=my-sql-db;Persist Security Info=False;User ID=admin;Password=secret;MultipleActiveResultSets=False;Encrypt=True;"

              :proxy-assets                      "https://www.cdn.com/assets/{path}"
              ;; this is not needed b/c we don't reference localhost in proxies.json in production
              :azure-function-disable-local-call "false"
              :azure-storage-conn-str            ""}
        }
 }
