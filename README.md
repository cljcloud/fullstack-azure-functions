# fullstack-azure-functions

Fullstack serverless app built on Azure Functions and Storage


## Running locally

Start app in dev mode (runs shadowcljs watch for both front-end and api build targets):

    $ lein shadow watch azure app

nrepl automatically started on 7002 port

Run 2 repl session in cursive, separate REPLs for browser and node.

Dev server is automaticallu started on 8020 port.

Start Azure Func (starts node process, required for REPL):

    $ cd target/azure && func start --cors * --port 8021    

or if restarted in the same folder after `lein clean`

    $ cd .. && cd azure && func start --cors * --port 8021

## Release Azure Functions

    $ lein with-profile prod shadow release azure app
    $ cp -rf node_modules target/azure

Publish to Azure Function

    $ cd target/azure
    $ func azure functionapp publish <FunctionAppName>

Fetch remote settings

    $ func azure functionapp fetch-app-settings <FunctionAppName>

Upload local settings

    $ func azure functionapp publish <FunctionAppName> --publish-settings-only

## Release client side assets

The client side assets must be uploaded to Azure storage under `/assets` folder.

## Azure Functions Bits

Using proxies to re-route static files to CDN storage.

   1. Allow wildcard route proxy - `"AZURE_FUNCTION_PROXY_BACKEND_URL_DECODE_SLASHES": "true"`

   2. Allow proxy to localhost - `"AZURE_FUNCTION_PROXY_DISABLE_LOCAL_CALL": "true"`
      
      *Note: This setting disable referencing to local functions.*

## Migrations

DB migration scripts inside `resources/migrations` folder.

Connection details should specified in local `profiles.clj` file.

To update your current DB to the latest migration run:

      $ lein migratus migrate  

To rollback the last migration:

      $ lein migratus rollback

To create a new migration:

      $ lein migratus create add-users-data


    

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

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
