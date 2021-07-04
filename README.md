# fullstack-azure-functions

Fullstack serverless app built on Azure Functions and Storage


## Running locally

Start app in dev mode (runs shadowcljs watch for both front-end and api build targets):

    $ lein shadow watch azure app

nrepl automatically started on 7002 port

Run 2 repl session in cursive, separate REPLs for browser and node.

Dev server is automaticallu started on 8020 port.

Start Azure Func (starts node process, required for REPL):

    $ cd target/azure && func start

## Release

    $ lein with-profile prod shadow release azure app

## Usage

FIXME: explanation

    $ java -jar fullstack-azure-functions-0.1.0-standalone.jar [args]

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
