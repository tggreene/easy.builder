# easy.builder

The idea here is simple, rather than start from templates or scaffolding cli
apps we can create a namespace in our project that we use to build up a project
piece by piece and then we can execute that namespace to create a new project.

This gives us iterative practises back at the very start of our projects with a
verbose record of what has been added.

## Structure

_Currently_

Middleware oriented core functionality with a large context map.

Operations to be carried out on the project are encoded as data with a field
`:operation` which can optionally be executed to carry out the operation and
receive a result. This essentially allows us to interactively approve operations
if required.
