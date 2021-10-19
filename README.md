# fullstack-azure-functions

Fullstack serverless app built on Azure Functions with Server Side Rendering.


## Running locally

Install Azure Functions Core Tools

`npm i -g azure-functions-core-tools@3`

Start app in dev mode (runs shadowcljs watch for both front-end and api build targets):

    $ lein shadow watch azure app

or

    $ lein watch

nrepl automatically started on 7002 port

Run 2 repl session in cursive, separate REPLs for browser and node.

Dev server is automaticallu started on 8020 port.

Start Azure Func (starts node process, required for REPL):

    $ cd target/azure && func start --cors * --port 8021    

or if restarted in the same folder after `lein clean`

    $ cd .. && cd azure && func start --cors * --port 8021

or

    $ lein azure

## Release Azure Functions

Clean up the target

    $ rm -rf target

Release shadow targets with production settings injected

    $ lein with-profile prod shadow release azure app

or

    $ lein release:prod

Copy over the node_modules, because we keep some packages from bundling,
due to their conflicts with shadow-cljs require, (e.g. `:keep-as-require #{"mssql"}`)

    $ cp -rf node_modules target/azure

Copy over the public folder content

    $ cp -rf resources/public/** target/app

Publish to Azure Function

With uploading the local settings file, 
if released with production profile, should contains production settings.

    $ cd target/azure
    $ func azure functionapp publish <FunctionAppName> --publish-local-settings

Additionally:

To fetch remote settings into local settings file (overwrites)

    $ func azure functionapp fetch-app-settings <FunctionAppName>

Just upload the local settings file (without code change)

    $ func azure functionapp publish <FunctionAppName> --publish-settings-only

## Release client side assets

(This should be done manually for now, via Azure Portal or any other way.)

The client side assets (everything from `target/app/assets`) must be uploaded to Azure storage under `/assets` public container.


## Azure Functions Bits

Using proxies to re-route static files to CDN storage.

   1. Allow wildcard route proxy - `"AZURE_FUNCTION_PROXY_BACKEND_URL_DECODE_SLASHES": "true"`

   2. Allow proxy to localhost - `"AZURE_FUNCTION_PROXY_DISABLE_LOCAL_CALL": "true"`
      
      *Note: This setting disable referencing to local functions.*

## Migrations

DB migration scripts inside `resources/migrations` folder.

Connection details should be specified in local `profiles.clj` file.

To update your current DB to the latest migration run:

      $ lein migratus migrate  

To rollback the last migration:

      $ lein migratus rollback

To create a new migration:

      $ lein migratus create add-users-data


## Examples

An example of the manual deployment process.

      $ rm -rf target
      $ lein with-profile prod shadow release azure app
      $ cp -rf node_modules target/azure
      $ cp -rf resources/public/** target/app
      $ cd target/azure
      $ func azure functionapp publish <FunctionAppName> --publish-local-settings



### Bugs

No, thanks.

## License

Copyright © 2021 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
